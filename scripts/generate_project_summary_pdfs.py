from __future__ import annotations

import os
import sys
from dataclasses import dataclass
from pathlib import Path


PDF_DEPS = Path("/tmp/codex-pdfdeps")
if PDF_DEPS.exists():
    sys.path.insert(0, str(PDF_DEPS))

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import inch
from reportlab.platypus import (
    ListFlowable,
    ListItem,
    PageBreak,
    Paragraph,
    Preformatted,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
)
from reportlab.graphics.shapes import Drawing, Line, Polygon, Rect, String
from reportlab.pdfbase.pdfmetrics import stringWidth
from pypdf import PdfReader


ROOT = Path(__file__).resolve().parents[1]
OUTPUT_DIR = ROOT / "output" / "pdf"
TMP_DIR = ROOT / "tmp" / "pdfs"


@dataclass(frozen=True)
class Note:
    title: str
    body: str


def ensure_dirs() -> None:
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    TMP_DIR.mkdir(parents=True, exist_ok=True)


def build_styles():
    styles = getSampleStyleSheet()
    styles.add(
        ParagraphStyle(
            name="TitleLarge",
            parent=styles["Title"],
            fontName="Helvetica-Bold",
            fontSize=26,
            leading=30,
            textColor=colors.HexColor("#0f172a"),
            alignment=TA_LEFT,
            spaceAfter=14,
        )
    )
    styles.add(
        ParagraphStyle(
            name="SectionTitle",
            parent=styles["Heading2"],
            fontName="Helvetica-Bold",
            fontSize=16,
            leading=20,
            textColor=colors.HexColor("#0f172a"),
            spaceBefore=10,
            spaceAfter=8,
        )
    )
    styles.add(
        ParagraphStyle(
            name="SubTitle",
            parent=styles["Heading3"],
            fontName="Helvetica-Bold",
            fontSize=12,
            leading=15,
            textColor=colors.HexColor("#1d4ed8"),
            spaceBefore=6,
            spaceAfter=4,
        )
    )
    styles.add(
        ParagraphStyle(
            name="Body",
            parent=styles["BodyText"],
            fontName="Helvetica",
            fontSize=10,
            leading=14,
            textColor=colors.HexColor("#1f2937"),
            spaceAfter=6,
        )
    )
    styles.add(
        ParagraphStyle(
            name="Small",
            parent=styles["BodyText"],
            fontName="Helvetica",
            fontSize=8.5,
            leading=11,
            textColor=colors.HexColor("#475569"),
            spaceAfter=4,
        )
    )
    styles.add(
        ParagraphStyle(
            name="CoverKicker",
            parent=styles["BodyText"],
            fontName="Helvetica-Bold",
            fontSize=10,
            leading=13,
            textColor=colors.HexColor("#2563eb"),
            spaceAfter=8,
        )
    )
    styles.add(
        ParagraphStyle(
            name="Caption",
            parent=styles["Italic"],
            fontName="Helvetica-Oblique",
            fontSize=8.5,
            leading=11,
            textColor=colors.HexColor("#64748b"),
            alignment=TA_CENTER,
            spaceAfter=4,
        )
    )
    styles.add(
        ParagraphStyle(
            name="CodeBlock",
            parent=styles["Code"],
            fontName="Courier",
            fontSize=8.5,
            leading=10.5,
            backColor=colors.HexColor("#eff6ff"),
            borderPadding=8,
            borderColor=colors.HexColor("#bfdbfe"),
            borderWidth=0.5,
            borderRadius=4,
            spaceAfter=8,
        )
    )
    return styles


def draw_header_footer(canvas, doc, title: str) -> None:
    page_w, page_h = A4
    canvas.saveState()
    canvas.setFillColor(colors.HexColor("#eff6ff"))
    canvas.rect(0, page_h - 42, page_w, 42, fill=1, stroke=0)
    canvas.setFillColor(colors.HexColor("#0f172a"))
    canvas.setFont("Helvetica-Bold", 9.5)
    canvas.drawString(doc.leftMargin, page_h - 26, title)
    canvas.setFillColor(colors.HexColor("#475569"))
    canvas.setFont("Helvetica", 8.5)
    canvas.drawRightString(page_w - doc.rightMargin, page_h - 26, f"Page {doc.page}")
    canvas.setStrokeColor(colors.HexColor("#cbd5e1"))
    canvas.setLineWidth(0.5)
    canvas.line(doc.leftMargin, 30, page_w - doc.rightMargin, 30)
    canvas.setFillColor(colors.HexColor("#64748b"))
    canvas.setFont("Helvetica", 8)
    canvas.drawString(doc.leftMargin, 18, "Generated from repository inspection on 2026-03-28")
    canvas.restoreState()


def paragraph(text: str, styles, style_name: str = "Body"):
    return Paragraph(text, styles[style_name])


def bullet_list(items: list[str], styles):
    flow_items = []
    for item in items:
        flow_items.append(ListItem(Paragraph(item, styles["Body"]), leftIndent=8))
    return ListFlowable(
        flow_items,
        bulletType="bullet",
        start="circle",
        leftIndent=16,
        bulletFontName="Helvetica",
        bulletFontSize=8,
    )


def info_table(rows: list[list[str]], styles, col_widths):
    table = Table(
        [[Paragraph(cell, styles["Body"]) for cell in row] for row in rows],
        colWidths=col_widths,
        hAlign="LEFT",
    )
    table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#dbeafe")),
                ("TEXTCOLOR", (0, 0), (-1, 0), colors.HexColor("#0f172a")),
                ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
                ("FONTSIZE", (0, 0), (-1, -1), 9),
                ("LEADING", (0, 0), (-1, -1), 12),
                ("BACKGROUND", (0, 1), (-1, -1), colors.white),
                ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#f8fafc")]),
                ("GRID", (0, 0), (-1, -1), 0.4, colors.HexColor("#cbd5e1")),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("LEFTPADDING", (0, 0), (-1, -1), 6),
                ("RIGHTPADDING", (0, 0), (-1, -1), 6),
                ("TOPPADDING", (0, 0), (-1, -1), 6),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
            ]
        )
    )
    return table


def cover_block(styles, title: str, subtitle: str, bullets: list[str]):
    elements = [
        Spacer(1, 1.1 * inch),
        paragraph("Repository-driven project documentation", styles, "CoverKicker"),
        paragraph(title, styles, "TitleLarge"),
        paragraph(subtitle, styles, "Body"),
        Spacer(1, 0.25 * inch),
        bullet_list(bullets, styles),
        Spacer(1, 0.35 * inch),
        paragraph(
            "Basis of summary: source code, build files, configuration, frontend routes, and successful local validation runs.",
            styles,
            "Small",
        ),
    ]
    return elements


def add_box(drawing: Drawing, x, y, w, h, title: str, lines: list[str], fill: str, stroke: str = "#1e293b"):
    drawing.add(
        Rect(
            x,
            y,
            w,
            h,
            rx=12,
            ry=12,
            fillColor=colors.HexColor(fill),
            strokeColor=colors.HexColor(stroke),
            strokeWidth=1.2,
        )
    )
    drawing.add(
        String(
            x + 10,
            y + h - 18,
            title,
            fontName="Helvetica-Bold",
            fontSize=11,
            fillColor=colors.HexColor("#0f172a"),
        )
    )
    current_y = y + h - 34
    for line in lines:
        drawing.add(
            String(
                x + 10,
                current_y,
                line,
                fontName="Helvetica",
                fontSize=8.8,
                fillColor=colors.HexColor("#1f2937"),
            )
        )
        current_y -= 12


def add_arrow(drawing: Drawing, x1, y1, x2, y2, color: str = "#334155"):
    drawing.add(Line(x1, y1, x2, y2, strokeColor=colors.HexColor(color), strokeWidth=1.5))
    dx = x2 - x1
    dy = y2 - y1
    length = (dx * dx + dy * dy) ** 0.5 or 1
    ux = dx / length
    uy = dy / length
    arrow_size = 6
    px = -uy
    py = ux
    p1 = (x2, y2)
    p2 = (x2 - ux * arrow_size - px * arrow_size / 2, y2 - uy * arrow_size - py * arrow_size / 2)
    p3 = (x2 - ux * arrow_size + px * arrow_size / 2, y2 - uy * arrow_size + py * arrow_size / 2)
    drawing.add(
        Polygon(
            [p1[0], p1[1], p2[0], p2[1], p3[0], p3[1]],
            fillColor=colors.HexColor(color),
            strokeColor=colors.HexColor(color),
        )
    )


def build_system_flowchart():
    drawing = Drawing(520, 360)
    add_box(
        drawing,
        20,
        248,
        145,
        82,
        "React client",
        ["Login / Register", "OAuth callback", "Dashboard / Profile", "Stores access token"],
        "#ecfeff",
    )
    add_box(
        drawing,
        190,
        248,
        150,
        82,
        "Security layer",
        ["CORS + stateless config", "JWT auth filter", "OAuth2 login handlers", "Role hierarchy"],
        "#eff6ff",
    )
    add_box(
        drawing,
        365,
        248,
        135,
        82,
        "Controllers",
        ["AuthController", "UserController", "DeveloperController", "HomeController"],
        "#f8fafc",
    )
    add_box(
        drawing,
        110,
        115,
        150,
        88,
        "Services",
        ["Registration + verification", "Refresh token rotation", "OAuth user merge", "Developer CRUD"],
        "#fefce8",
    )
    add_box(
        drawing,
        285,
        115,
        170,
        88,
        "Persistence",
        ["JPA repositories", "users / roles", "refresh_tokens", "email_verification_tokens / developers"],
        "#f0fdf4",
    )
    add_box(
        drawing,
        180,
        16,
        190,
        64,
        "External systems",
        ["Google / GitHub OAuth", "Gmail SMTP", "MySQL database"],
        "#fdf2f8",
    )
    add_arrow(drawing, 165, 289, 190, 289)
    add_arrow(drawing, 340, 289, 365, 289)
    add_arrow(drawing, 432, 248, 392, 203)
    add_arrow(drawing, 270, 248, 215, 203)
    add_arrow(drawing, 260, 158, 285, 158)
    add_arrow(drawing, 355, 115, 320, 80)
    add_arrow(drawing, 210, 115, 230, 80)
    return drawing


def build_local_auth_flowchart():
    drawing = Drawing(520, 420)
    add_box(drawing, 25, 332, 120, 58, "1. Register", ["POST /auth/register", "email + password"], "#ecfeff")
    add_box(drawing, 200, 332, 135, 58, "2. User creation", ["hash password", "assign ROLE_USER"], "#eff6ff")
    add_box(drawing, 375, 332, 120, 58, "3. Verification", ["token stored", "email event published"], "#fefce8")

    add_box(drawing, 25, 225, 120, 70, "4. Login", ["AuthenticationManager", "CustomUserDetails"], "#ecfeff")
    add_box(drawing, 200, 225, 135, 70, "5. Token issue", ["access JWT", "refresh JWT", "httpOnly cookie"], "#f0fdf4")
    add_box(drawing, 375, 225, 120, 70, "6. Session store", ["hash refresh token", "save IP / agent", "revocation state"], "#fdf2f8")

    add_box(drawing, 25, 108, 120, 72, "7. API request", ["Bearer access token", "JWT filter validates"], "#ecfeff")
    add_box(drawing, 200, 108, 135, 72, "8. Refresh path", ["POST /auth/refresh", "rotate token pair"], "#eff6ff")
    add_box(drawing, 375, 108, 120, 72, "9. Session control", ["logout", "logout-all", "revoke session"], "#fef2f2")

    add_arrow(drawing, 145, 361, 200, 361)
    add_arrow(drawing, 335, 361, 375, 361)
    add_arrow(drawing, 85, 332, 85, 295)
    add_arrow(drawing, 267, 332, 267, 295)
    add_arrow(drawing, 435, 332, 435, 295)
    add_arrow(drawing, 145, 260, 200, 260)
    add_arrow(drawing, 335, 260, 375, 260)
    add_arrow(drawing, 85, 225, 85, 180)
    add_arrow(drawing, 267, 225, 267, 180)
    add_arrow(drawing, 435, 225, 435, 180)
    add_arrow(drawing, 145, 144, 200, 144)
    add_arrow(drawing, 335, 144, 375, 144)
    return drawing


def build_oauth_flowchart():
    drawing = Drawing(520, 300)
    add_box(drawing, 20, 198, 125, 68, "Frontend", ["user clicks Google/GitHub", "browser redirects to backend"], "#ecfeff")
    add_box(drawing, 195, 198, 135, 68, "Spring OAuth2", ["redirect to provider", "exchange auth code for profile"], "#eff6ff")
    add_box(drawing, 375, 198, 120, 68, "Provider", ["Google or GitHub", "returns identity data"], "#fdf2f8")
    add_box(drawing, 105, 62, 140, 82, "CustomOAuthUserService", ["normalize profile", "match by email", "create/update local user"], "#fefce8")
    add_box(drawing, 295, 62, 160, 82, "OAuthSuccessHandler", ["issue local JWT pair", "set refresh cookie", "redirect with access token"], "#f0fdf4")
    add_arrow(drawing, 145, 232, 195, 232)
    add_arrow(drawing, 330, 232, 375, 232)
    add_arrow(drawing, 435, 198, 380, 144)
    add_arrow(drawing, 262, 198, 205, 144)
    add_arrow(drawing, 245, 103, 295, 103)
    return drawing


def build_summary_pdf(out_path: Path) -> None:
    styles = build_styles()
    doc = SimpleDocTemplate(
        str(out_path),
        pagesize=A4,
        leftMargin=42,
        rightMargin=42,
        topMargin=60,
        bottomMargin=40,
        title="BackendForge Project Summary",
        author="Codex",
    )
    story = []
    story.extend(
        cover_block(
            styles,
            "BackendForge Project Summary",
            "A code-driven summary of what the repository currently implements, how the major pieces fit together, and where the implementation differs from the broader product vision in the README.",
            [
                "Primary app: Spring Boot backend with JWT, OAuth2, refresh-token sessions, email verification, and a protected Developer CRUD module.",
                "Client app: React + Vite frontend for registration, login, OAuth callback handling, session visibility, profile display, and logout controls.",
                "Validation status: backend test task passed; frontend production build passed during this documentation run.",
            ],
        )
    )

    story.append(Spacer(1, 0.2 * inch))
    story.append(paragraph("Project At A Glance", styles, "SectionTitle"))
    story.append(
        paragraph(
            "Despite the README describing a future AI-powered learning platform, the current repository is best understood as an authentication-heavy backend application with a matching frontend shell. The strongest implemented capabilities are account creation, JWT issuance, refresh-token rotation, OAuth sign-in, session revocation, and a small role-protected Developer management API.",
            styles,
        )
    )
    story.append(
        info_table(
            [
                ["Area", "Current implementation"],
                ["Backend", "Java 21 + Spring Boot 3.5, Spring Security, Spring Data JPA, MySQL, JavaMail, OAuth2 Client"],
                ["Frontend", "React 19 + Vite + React Router + Axios + Framer Motion"],
                ["Primary domain", "Users, roles, refresh-token sessions, email verification tokens, and developers"],
                ["Security posture", "Stateless API, access token in frontend storage, refresh token in httpOnly cookie, role-based authorization"],
                ["Auxiliary module", "DeveloperSecurity Gradle app appears to be a separate starter/stub and is not wired into the main application flow"],
            ],
            styles,
            [110, 360],
        )
    )

    story.append(paragraph("Repository Structure", styles, "SectionTitle"))
    story.append(
        bullet_list(
            [
                "<b>Root Spring Boot app</b>: all production backend code lives under <font name='Courier'>src/main/java/com/aditya/simple_web_app/web_app</font>.",
                "<b>Frontend app</b>: the browser client lives under <font name='Courier'>backendforge-ui/</font> and targets the backend at <font name='Courier'>http://localhost:8080</font>.",
                "<b>DeveloperSecurity/</b>: a second Spring Boot project with almost no business code, likely kept as an experiment or a future sandbox.",
                "<b>Configuration</b>: <font name='Courier'>application.properties</font> contains datasource, JWT, mail, and OAuth client settings.",
            ],
            styles,
        )
    )

    story.append(paragraph("Core Runtime Components", styles, "SectionTitle"))
    component_rows = [
        ["Component", "Responsibility"],
        ["WebSecurityConfig", "Builds a stateless Spring Security filter chain, enables CORS, disables form/basic auth, wires OAuth2 login, and installs the JWT filter."],
        ["JwtAuthFilter", "Extracts Bearer access tokens, validates them, loads the user, and fills the Spring security context."],
        ["AuthController", "Handles registration, login, refresh, logout, logout-all, session listing, and session revocation."],
        ["UserController", "Returns the authenticated user profile for the frontend profile page."],
        ["DeveloperController", "Exposes role-protected CRUD endpoints for the Developer entity, including pagination and partial updates."],
        ["RefreshTokenService", "Hashes refresh tokens before persistence, validates them, supports reuse detection, and revokes one or all sessions."],
        ["CustomOAuthUserService + OAuthSuccessHandler", "Convert Google/GitHub identities into local users and issue the platform's own JWT pair after OAuth success."],
        ["UserRegisteredEventListener", "Sends verification emails asynchronously after registration."],
    ]
    story.append(info_table(component_rows, styles, [140, 330]))

    story.append(PageBreak())
    story.append(paragraph("Data Model", styles, "SectionTitle"))
    story.append(
        paragraph(
            "The data layer is centered on user identity and session control. Roles are eager-loaded onto the user, refresh tokens are stored as hashes, and email verification is modeled as a one-to-one token record. The Developer table is independent from the auth model and acts like a protected sample domain.",
            styles,
        )
    )
    story.append(
        info_table(
            [
                ["Entity", "Meaning in the system"],
                ["User", "Canonical account record. Stores email, optional password, optional profile data, auth provider type, flags, timestamps, and roles."],
                ["Role", "Simple authority table containing values like ROLE_USER and ROLE_ADMIN."],
                ["RefreshToken", "Per-session persistence record for hashed refresh tokens, revocation state, timestamps, user agent, IP address, and UUID session id."],
                ["EmailVerificationToken", "One-to-one token record for email verification, storing only the hash and expiry state."],
                ["Developer", "Separate business entity with name and role fields, managed through protected CRUD endpoints."],
            ],
            styles,
            [120, 350],
        )
    )
    story.append(Spacer(1, 0.12 * inch))
    story.append(paragraph("Major User-Facing Behavior", styles, "SectionTitle"))
    story.append(
        bullet_list(
            [
                "Visitors can register with email and password, attempt local login, or start Google/GitHub OAuth from the React login page.",
                "After successful login, the client stores an access token in local storage and relies on an httpOnly cookie for refresh.",
                "Protected frontend pages are the dashboard and profile views. The dashboard reads active sessions from the backend and can revoke them individually. The profile screen shows identity data and can revoke all sessions.",
                "The frontend uses a global Axios interceptor to retry failed authenticated requests by calling <font name='Courier'>/auth/refresh</font> and then replaying the original request.",
                "Developer CRUD exists only on the API side right now; there is no dedicated frontend screen for it in this repository.",
            ],
            styles,
        )
    )

    story.append(paragraph("Security Model", styles, "SectionTitle"))
    story.append(
        paragraph(
            "The design intention is solid: short-lived access tokens, refresh tokens scoped to cookie-based refresh, hashed refresh-token persistence, role-based route protection, and a dedicated session list with selective revocation. The runtime model is entirely stateless at the HTTP layer, with persistence used only for user records and refresh-token state.",
            styles,
        )
    )
    story.append(
        info_table(
            [
                ["Security mechanism", "How it is used here"],
                ["Access token", "Bearer JWT used on normal API requests and attached by the frontend Axios interceptor."],
                ["Refresh token", "Stored in a cookie on the <font name='Courier'>/auth/refresh</font> path and rotated during refresh."],
                ["Authorization", "Endpoint access is enforced through request matchers and <font name='Courier'>@PreAuthorize</font> annotations."],
                ["Role hierarchy", "ROLE_ADMIN inherits ROLE_USER permissions."],
                ["OAuth account linking", "OAuth identities are matched to local users by email, so the same email lands on one account."],
            ],
            styles,
            [120, 350],
        )
    )

    story.append(paragraph("Important Implementation Notes", styles, "SectionTitle"))
    notes = [
        Note(
            "Current product reality vs README vision",
            "The README markets BackendForge as an AI learning platform, but the codebase today is mostly an authentication and session-management system plus a small Developer CRUD example. No quiz engine, adaptive learning path, or AI tutoring workflow is present in the inspected code.",
        ),
        Note(
            "Email verification flow is partially wired but inconsistent",
            "The controller expects verification at /auth/verify/{token}, while the email listener currently builds a link using a query parameter style /auth/verify?token=... . That means the emailed link does not match the implemented controller route as written.",
        ),
        Note(
            "Registration currently ignores the frontend name field",
            "The React register form submits name, email, and password, but the backend registration DTO only accepts email and password. As a result, locally registered users are created without a stored display name.",
        ),
        Note(
            "Verification is not currently enforcing login readiness",
            "New users are created with enabled=true during registration. Email verification still exists and sets emailVerified=true later, but the login gate relies on enabled status rather than emailVerified, so the current flow does not strictly block pre-verification login.",
        ),
        Note(
            "HS256 is the real token engine right now",
            "The repository includes an RS256 token service class, but it is only a stub behind a profile. The active implementation is the HS256 token service marked as primary.",
        ),
        Note(
            "Cookie behavior differs between local login and OAuth login",
            "Local login sets refresh cookies with secure=true, while OAuth success sets secure=false for local development. That inconsistency can produce different browser behavior during development on plain HTTP.",
        ),
        Note(
            "Sensitive configuration is stored directly in application.properties",
            "Datasource credentials, JWT secrets, and mail credentials are present in plain configuration. For a real deployment, those values should move into environment variables or a secret manager.",
        ),
    ]
    for note in notes:
        story.append(paragraph(note.title, styles, "SubTitle"))
        story.append(paragraph(note.body, styles))

    story.append(paragraph("Validation Performed For This Summary", styles, "SectionTitle"))
    story.append(
        bullet_list(
            [
                "<b>Backend</b>: <font name='Courier'>./mvnw -q test</font> completed successfully.",
                "<b>Frontend</b>: <font name='Courier'>npm run build</font> completed successfully inside <font name='Courier'>backendforge-ui/</font>.",
                "<b>PDF generation</b>: this document and the companion flow guide were generated locally and checked for readable page text and page counts.",
            ],
            styles,
        )
    )

    doc.build(
        story,
        onFirstPage=lambda canvas, doc: draw_header_footer(canvas, doc, "BackendForge Project Summary"),
        onLaterPages=lambda canvas, doc: draw_header_footer(canvas, doc, "BackendForge Project Summary"),
    )


def build_flow_pdf(out_path: Path) -> None:
    styles = build_styles()
    doc = SimpleDocTemplate(
        str(out_path),
        pagesize=A4,
        leftMargin=42,
        rightMargin=42,
        topMargin=60,
        bottomMargin=40,
        title="BackendForge Flow Guide",
        author="Codex",
    )
    story = []
    story.extend(
        cover_block(
            styles,
            "BackendForge Flow Guide",
            "Detailed flow explanations for the runtime paths that matter most in the current repository: local registration, local login, refresh rotation, OAuth sign-in, protected API usage, and Developer CRUD access.",
            [
                "Page 1-2: system map and local auth lifecycle.",
                "Page 3: OAuth flow and user-account linking.",
                "Page 4+: API flow details, frontend routing behavior, and operational observations.",
            ],
        )
    )
    story.append(PageBreak())

    story.append(paragraph("Overall System Flow", styles, "SectionTitle"))
    story.append(paragraph("This diagram shows the broad request path from browser to persistence and external integrations.", styles))
    story.append(build_system_flowchart())
    story.append(paragraph("System-level flowchart derived from the running backend and frontend code paths.", styles, "Caption"))

    story.append(paragraph("Runtime Sequence", styles, "SectionTitle"))
    story.append(
        Preformatted(
            "\n".join(
                [
                    "Browser -> React route or form",
                    "React -> Axios request to http://localhost:8080",
                    "Spring Security -> CORS / stateless policy / JWT filter / OAuth handling",
                    "Controller -> service layer -> JPA repository",
                    "Repository -> MySQL persistence",
                    "Optional side effects -> Gmail SMTP or OAuth providers",
                ]
            ),
            styles["CodeBlock"],
        )
    )

    story.append(PageBreak())
    story.append(paragraph("Local Registration, Login, and Refresh Lifecycle", styles, "SectionTitle"))
    story.append(paragraph("The local auth path is the most complete and most security-focused part of the repository.", styles))
    story.append(build_local_auth_flowchart())
    story.append(paragraph("Local authentication and session lifecycle.", styles, "Caption"))

    flow_rows = [
        ["Step", "What the code does"],
        ["Register", "The backend creates a user, hashes the password with BCrypt, assigns ROLE_USER, generates an email verification token, and emits an async event to send mail."],
        ["Login", "Spring AuthenticationManager validates credentials through CustomUserDetailsService, then the controller issues an access token and refresh token."],
        ["Session persistence", "The refresh token is hashed with SHA-256 and stored with user, IP address, user agent, expiry, and revocation state."],
        ["Refresh", "The backend reads the refresh cookie, validates the JWT and stored hash, revokes the old record, creates a new refresh token, and returns a new access token."],
        ["Logout", "Single logout revokes the current refresh token. Logout-all revokes every active refresh-token record for the user."],
        ["Session list", "The dashboard reads active refresh-token records and can revoke one by session UUID."],
    ]
    story.append(info_table(flow_rows, styles, [90, 380]))

    story.append(PageBreak())
    story.append(paragraph("OAuth Login and Account Linking", styles, "SectionTitle"))
    story.append(
        paragraph(
            "OAuth is treated as a second entry point into the same JWT-based platform. After provider authentication, the backend still issues its own access and refresh tokens.",
            styles,
        )
    )
    story.append(build_oauth_flowchart())
    story.append(paragraph("OAuth path from provider redirect to local JWT issuance.", styles, "Caption"))
    story.append(
        bullet_list(
            [
                "The login page starts OAuth by redirecting the browser to Spring Security's provider authorization URL.",
                "Spring Security exchanges the authorization code with Google or GitHub and fetches profile attributes.",
                "CustomOAuthUserService normalizes provider data, then looks up the user by email. Existing users are updated; missing users are created with ROLE_USER.",
                "OAuthSuccessHandler issues the platform's own JWT pair, stores the refresh token server-side as a hash, sets a refresh cookie, and redirects the user back to the React callback route with the access token in the URL.",
                "The React OAuth callback page saves the access token into local storage, marks the app as authenticated, and navigates to the dashboard.",
            ],
            styles,
        )
    )

    story.append(PageBreak())
    story.append(paragraph("Protected Request Flow", styles, "SectionTitle"))
    story.append(
        bullet_list(
            [
                "React stores the access token in local storage and automatically attaches it as a Bearer token through the Axios request interceptor.",
                "JwtAuthFilter extracts the token, reads the username subject, loads the user from the database, validates the token, and installs an authenticated principal into the security context.",
                "Once the security context is populated, normal Spring request authorization decides whether the endpoint can be reached.",
                "If an API call returns 401, the Axios response interceptor attempts one refresh call. On success it stores the new access token and retries the original request automatically.",
                "If refresh also fails, the client clears local storage and returns the browser to the login screen.",
            ],
            styles,
        )
    )

    story.append(paragraph("Frontend Route Flow", styles, "SectionTitle"))
    story.append(
        info_table(
            [
                ["Route", "Behavior"],
                ["/login", "Shows the local login form and Google/GitHub buttons unless already authenticated."],
                ["/register", "Collects name, email, and password, then calls the backend register endpoint."],
                ["/oauth/callback", "Reads the access token from the URL after OAuth success and stores it in local storage."],
                ["/dashboard", "Protected route that loads active sessions from /auth/sessions and allows revocation."],
                ["/profile", "Protected route that loads /user/me and offers logout-all."],
            ],
            styles,
            [90, 380],
        )
    )

    story.append(paragraph("Developer CRUD Flow", styles, "SectionTitle"))
    story.append(
        bullet_list(
            [
                "The Developer entity is a separate domain model with only id, name, and role.",
                "Create, update, patch, and delete operations require ROLE_ADMIN.",
                "Read operations require either ROLE_USER or ROLE_ADMIN.",
                "Pagination is offset-based at the API boundary but translated internally into Spring Data page requests.",
                "This CRUD capability is not currently surfaced in the React UI, so it behaves like an API-only protected resource.",
            ],
            styles,
        )
    )

    story.append(paragraph("Operational Observations", styles, "SectionTitle"))
    story.append(
        bullet_list(
            [
                "Role seeding happens automatically on application startup through RoleInitializer.",
                "Expired refresh tokens are cleaned up daily at 3 AM by a scheduled task.",
                "The dashboard currently expects device names, but the refresh-token creation flow stores user agent and IP rather than a resolved device name field.",
                "The extra DeveloperSecurity module is not part of the main runtime path described above.",
            ],
            styles,
        )
    )

    doc.build(
        story,
        onFirstPage=lambda canvas, doc: draw_header_footer(canvas, doc, "BackendForge Flow Guide"),
        onLaterPages=lambda canvas, doc: draw_header_footer(canvas, doc, "BackendForge Flow Guide"),
    )


def validate_pdf(path: Path) -> tuple[int, str]:
    reader = PdfReader(str(path))
    page_count = len(reader.pages)
    sample = []
    for page in reader.pages[:2]:
        text = page.extract_text() or ""
        sample.append(text[:600])
    return page_count, "\n".join(sample)


def main() -> int:
    ensure_dirs()
    summary_path = OUTPUT_DIR / "backendforge_project_summary.pdf"
    flow_path = OUTPUT_DIR / "backendforge_flow_guide.pdf"

    build_summary_pdf(summary_path)
    build_flow_pdf(flow_path)

    for pdf_path in [summary_path, flow_path]:
        pages, sample = validate_pdf(pdf_path)
        if pages <= 0 or len(sample.strip()) < 100:
            raise RuntimeError(f"PDF validation failed for {pdf_path}")
        print(f"Generated {pdf_path} ({pages} pages)")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
