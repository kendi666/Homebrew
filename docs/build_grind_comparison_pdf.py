#!/usr/bin/env python3
"""Briefing PDF: four K-Ultra grind maps, for review by another model."""

from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm
from reportlab.lib.colors import Color, white, black
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_LEFT, TA_JUSTIFY
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle,
    KeepTogether, HRFlowable,
)

OUT = "/Users/kendi/Documents/Dev/Brew/docs/k-ultra-grind-convert-comparison.pdf"

INK = Color(0.12, 0.13, 0.15)
MUTED = Color(0.33, 0.36, 0.40)
RULE = Color(0.82, 0.84, 0.86)
HEAD = Color(0.16, 0.22, 0.20)
HEAD_FG = white
ZEBRA = Color(0.96, 0.97, 0.96)
BAND = Color(0.93, 0.95, 0.93)
WARN = Color(0.45, 0.22, 0.12)


def styles():
    base = getSampleStyleSheet()
    base.add(ParagraphStyle(
        name="Cover", fontName="Times-Bold", fontSize=18, leading=22,
        textColor=INK, spaceAfter=4,
    ))
    base.add(ParagraphStyle(
        name="Deck", fontName="Times-Italic", fontSize=11, leading=14,
        textColor=MUTED, spaceAfter=8,
    ))
    base.add(ParagraphStyle(
        name="H", fontName="Times-Bold", fontSize=13, leading=16,
        textColor=INK, spaceBefore=12, spaceAfter=4,
    ))
    base.add(ParagraphStyle(
        name="Body", fontName="Times-Roman", fontSize=10, leading=13.5,
        textColor=INK, alignment=TA_JUSTIFY, spaceAfter=6,
    ))
    base.add(ParagraphStyle(
        name="Cell", fontName="Times-Roman", fontSize=8, leading=10.5,
        textColor=INK,
    ))
    base.add(ParagraphStyle(
        name="CellB", fontName="Times-Bold", fontSize=8, leading=10.5,
        textColor=HEAD_FG,
    ))
    base.add(ParagraphStyle(
        name="Small", fontName="Times-Roman", fontSize=8.5, leading=11,
        textColor=MUTED, spaceAfter=4,
    ))
    base.add(ParagraphStyle(
        name="Q", fontName="Times-Roman", fontSize=10, leading=13.5,
        textColor=INK, leftIndent=8, spaceAfter=4,
    ))
    return base


S = styles()


def P(text, style="Body"):
    return Paragraph(text, S[style])


def cell(text, header=False):
    return Paragraph(text, S["CellB"] if header else S["Cell"])


def table(headers, rows, widths):
    data = [[cell(h, header=True) for h in headers]]
    for row in rows:
        data.append([cell(c) for c in row])
    t = Table(data, colWidths=widths, repeatRows=1)
    style = [
        ("BACKGROUND", (0, 0), (-1, 0), HEAD),
        ("TEXTCOLOR", (0, 0), (-1, 0), HEAD_FG),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 4),
        ("RIGHTPADDING", (0, 0), (-1, -1), 4),
        ("TOPPADDING", (0, 0), (-1, -1), 4),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
        ("GRID", (0, 0), (-1, -1), 0.3, RULE),
    ]
    for i in range(1, len(data)):
        if i % 2 == 0:
            style.append(("BACKGROUND", (0, i), (-1, i), ZEBRA))
    t.setStyle(TableStyle(style))
    return t


def footer(canvas, doc):
    canvas.saveState()
    canvas.setStrokeColor(RULE)
    canvas.setLineWidth(0.4)
    canvas.line(18 * mm, 12 * mm, A4[0] - 18 * mm, 12 * mm)
    canvas.setFont("Times-Roman", 8)
    canvas.setFillColor(MUTED)
    canvas.drawString(18 * mm, 7 * mm, "K-Ultra grind maps  ·  12 November 2026  ·  for review, not a lab result")
    canvas.drawRightString(A4[0] - 18 * mm, 7 * mm, str(doc.page))
    canvas.restoreState()


def build():
    doc = SimpleDocTemplate(
        OUT, pagesize=A4,
        leftMargin=16 * mm, rightMargin=16 * mm,
        topMargin=14 * mm, bottomMargin=16 * mm,
        title="Four K-Ultra grind maps compared",
        author="Homebrew",
    )
    W = A4[0] - 32 * mm
    story = []

    story.append(P("Four K-Ultra grind maps", "Cover"))
    story.append(P(
        "A briefing so another model can judge the same numbers. "
        "None of the four is a measured particle-size calibration.",
        "Deck",
    ))
    story.append(P(
        "The app in this comparison is Homebrew (package com.brewmaster). "
        "Its Grind Convert stores one estimated-particle micron, called M here, "
        "and converts every grinder through that number. The question at the end "
        "is the one to answer. Do not treat Homebrew as correct because this file exists."
    ))

    story.append(P("1. Facts that are not in dispute", "H"))
    story.append(P(
        "The 1Zpresso K-Ultra moves the cone <b>20 micron axially per click</b>. "
        "There are 10 clicks per printed number and 100 clicks per rotation. "
        "App dial 8.1 means number 8, click 1, which is 81 clicks from zero. "
        "Honest and Cuppd write the same setting as 0.8.1. "
        "Beean writes it as 0 rotation, 8 number, 1 click."
    ))
    story.append(P(
        "That 20 micron is burr travel, not the size of the grounds. "
        "Beean's own K-Ultra page says the same thing: the particle change per click "
        "is smaller than 20 micron, and a change is usually tasted at 2 to 3 clicks. "
        "One full number on the dial is 10 clicks."
    ))
    story.append(P(
        "The factory chart (K-Ultra Grind Setting Reference, 31 Oct 2022) is a dial chart. "
        "It publishes no microns. Read approximately: Turkish 2.0–3.5, espresso 3.2–4.5, "
        "AeroPress / moka / drip 5.2–7.3, siphon / pour-over 8.0–9.0, French press 9.2–10. "
        "Zero is where the handle starts to resist, and 1Zpresso says a unit can sit "
        "about 2 clicks off the printed 0."
    ))

    story.append(P("2. The four maps", "H"))
    story.append(table(
        ["Map", "What it does with a K-Ultra click", "What it is not"],
        [
            [
                "Homebrew Grind Convert",
                "M = 100 × dial + 50. That is 10 micron per click, plus 50 micron at dial 0. Dial 8.0 = 850 micron. Comandante C40 is M = 30 × clicks, so 28 clicks = 840 micron and lands at dial 7.9.",
                "Not the 20 micron axial step. Not fitted to a refractometer.",
            ],
            [
                "Coffee Notes",
                "Clicks = micron / step, zero at 0. K-Ultra step 20 micron, C40 step 30 micron. 600 micron = 30 clicks = dial 3.0. 840 micron = 42 clicks = dial 4.2.",
                "This micron is burr travel. Their pour-over bar of 500–1200 micron is dial 2.5–6.0, which the factory chart still calls espresso through drip.",
            ],
            [
                "Honest, as used by Cuppd",
                "One rotation (100 clicks, written 0.0.0–1.0.0) is given as 0–760 micron, about 7.6 micron per click from zero. Cuppd's published cells: 600 micron = 0.7.9; V60 650 micron = 0.8.6 and C40 24 clicks; pour-over 700 micron = 0.9.2 and C40 26 clicks.",
                "Not a line for one recipe. Honest's V60 row is the wide band dial 5.3–9.2. C40 28 is not a published Cuppd cell; extending their table lands near dial 9.8–10.0.",
            ],
            [
                "Beean converter, observed",
                "Two screens, read as dials only. Target 600 micron came back as 0 rotation, 6 number, 3 click, which is dial 6.3 (63 clicks). Comandante C40 at 28 clicks came back as number 8, click 9, which is dial 8.9 (89 clicks). Beean says the converter is crowdsourced and is a starting point.",
                "No published formula was found. A micron figure shown beside the 28-click screen is ignored here, on instruction, and is not used to infer a slope.",
            ],
        ],
        [32 * mm, 78 * mm, 68 * mm],
    ))
    story.append(Spacer(1, 4))
    story.append(P(
        "Homebrew's other grinders share the same M. FM 120 is M = 75 × dial + 50. "
        "Timemore Sculptor is M = 150 × dial − 250. Guerrero GR64 is M = 11 × dial + 150. "
        "Changing only the K-Ultra slope would make those conversions disagree with K-Ultra.",
        "Small",
    ))

    story.append(P("3. Same input, four dials", "H"))
    story.append(P(
        "Dial numbers below are K-Ultra. A blank means that map did not publish the cell "
        "and it was not extrapolated."
    ))
    story.append(table(
        ["Input", "Homebrew", "Coffee Notes", "Honest / Cuppd", "Beean, observed"],
        [
            ["C40, 28 clicks (Devil's recipe, written ~840 micron)",
             "840 micron, dial 7.9",
             "Dial 4.2 (42 × 20)",
             "Not published. Extended from their 24 and 26 click cells: about dial 9.8–10.0",
             "Dial 8.9"],
            ["600 micron",
             "Dial 5.5 (55 clicks). (600 − 50) / 10 = 55.",
             "Dial 3.0 (30 clicks)",
             "Dial 7.9, their filter-machine row",
             "Dial 6.3"],
            ["700 micron",
             "Dial 6.5",
             "Dial 3.5",
             "Dial 9.2, their pour-over row",
             "Not observed"],
            ["800 micron",
             "Dial 7.5",
             "Dial 4.0",
             "Not in the published table",
             "Not observed"],
            ["Factory pour-over, dial 8.0",
             "850 micron",
             "1,600 micron of axial travel",
             "About 608 micron if 7.6 micron × 80 clicks",
             "Their written V60 start is dial 8.0. The converter was not asked for this dial."],
            ["Factory pour-over, dial 9.0",
             "950 micron",
             "1,800 micron of axial travel",
             "About 684 micron if 7.6 × 90. Near their 700 micron / 0.9.2 cell.",
             "Inside their written V60 range of 7.0–9.5"],
        ],
        [32 * mm, 36 * mm, 34 * mm, 40 * mm, 36 * mm],
    ))

    story.append(P("4. Where each map puts a real recipe", "H"))
    story.append(P(
        "<b>Devil's recipe</b> (Coffee Notes, Kasuya improved). 20 g / 300 g. "
        "Grind line: ~840 micron, called Coarse, 28 clicks on a Comandante C40. "
        "Homebrew starts at dial 7.9. Coffee Notes' own 20 micron step would say dial 4.2, "
        "which is espresso on the factory chart. Beean's converter said dial 8.9. "
        "Cuppd does not publish this cell; the extension of their table is about dial 9.8–10.0."
    ))
    story.append(P(
        "<b>An outside chart, not one of the four.</b> NNSCoffee's comparison puts "
        "C40 24–28 clicks on K-Ultra 7.0–8.0, and C40 28–32 on 8.0–9.0. "
        "On that chart, 28 clicks is the boundary at dial 8.0. Their separate anchor is "
        "C40 25 clicks ≈ K-Ultra 7.2–7.5. That is one click from Homebrew's 7.9, "
        "and ten clicks finer than Beean's 8.9."
    ))
    story.append(P(
        "<b>Archer fermented naturals</b> (Coffee Notes). Grind line: 700–800 micron, called Medium. "
        "Homebrew: dial 6.5–7.5. Coffee Notes' 20 micron step: dial 3.5–4.0. "
        "Dividing 700–800 by 10, with no offset, is dial 7.0–8.0. That division is not "
        "Homebrew's formula. Homebrew's 700 micron is dial 6.5 because of the +50 offset."
    ))

    story.append(P("5. What Homebrew decided, and what it did not decide", "H"))
    story.append(P(
        "Homebrew kept M = 100 × dial + 50. The reasons were consistency, not a proof that "
        "850 micron is the true size at dial 8.0."
    ))
    bullets = [
        "The 20 micron axial step is not stored as particle size. Doing that puts a 600 micron pour-over note at dial 3.0, inside the factory espresso band.",
        "Honest's V60 band of dial 5.3–9.2 is kept as an outer envelope. A cup outside it is off the crowd. The band is not the recommended dial, because dial 8.0 sits inside many Honest rows at once.",
        "The 0–760 micron scale is not imported into Grind Convert. One rotation is 2,000 micron of axial travel, so 760 micron is about 7.6 micron of their particle unit per click. That unit is not the same M as FM 120 and the Sculptor.",
        "A factory-anchored alternative, M = 96.8 × dial − 23, was calculated and not adopted. It puts espresso mid-dial 3.85 near 350 micron and pour-over mid-dial 8.5 near 800 micron. The gap versus 100 × dial + 50 is about 100 micron, which is inside the noise of a later refractometer check, and it would move FM 120 and the Sculptor.",
        "The dashboard no longer labels a filter dial by 20 micron × clicks. K-Ultra filter bands are: 6.5–8.0 Medium-Fine, 8.0–8.6 Medium, 8.6–9.2 Medium-Coarse, 9.2–9.5 Coarse, 9.5–10.0 Extra-Coarse. So 7.3 is Medium-Fine, 7.9 is Medium-Fine, 8.5 is Medium.",
    ]
    for b in bullets:
        story.append(P("- " + b, "Q"))
    story.append(P(
        "So Homebrew's number is the one the app will keep until a measurement says otherwise. "
        "It is not the claim that it is the true particle size. "
        "Beean at dial 8.9 and the Cuppd extension near dial 10 are 10 to 20 clicks coarser "
        "than dial 7.9. Beean says 2 to 3 clicks is already a tasted change, so those are "
        "different cups, not rounding."
    ))

    story.append(P("6. What would actually decide it", "H"))
    story.append(P(
        "A refractometer does not read burr gap. Extraction yield is "
        "EY = brew TDS × beverage weight / dose. On 15 g coffee and 250 g beverage, "
        "a TDS error of 0.02 is about 0.3 EY, and 0.05 is about 0.8 EY. "
        "A zero that is 2 clicks off is 0.2 of a dial. Predicting EY from a gap in microns "
        "is not available from any of these four maps."
    ))
    story.append(P(
        "The check that would falsify a starting dial: lock one recipe, one coffee, one dose, "
        "one pour, and one temperature. Brew dial 8.0 and dial 9.0. "
        "k = (EY at 8.0 − EY at 9.0) / (8.0 − 9.0). "
        "Later, EY(dial) ≈ EY(8.5) + k × (dial − 8.5). "
        "Store the dial and the EY. Do not store a gap micron as if it were the grounds."
    ))

    story.append(P("7. Questions for the reviewing model", "H"))
    story.append(P(
        "Answer from the numbers in this file. If a figure was marked "
        "“not published” or “not observed”, do not fill it in. "
        "Do not invent a new micron-per-click."
    ))
    questions = [
        "For a recipe that says Comandante C40, 28 clicks, which K-Ultra dial would you start on: 4.2, 7.9, 8.9, or about 9.8–10.0? Why that one, and why not the other three?",
        "For a recipe that says 600 micron and does not name a grinder, which dial would you start on: 3.0, 5.5, 6.3, or 7.9? State which meaning of “micron” you are assuming.",
        "Should the printed 20 micron per click ever be used as the particle size inside a converter that also contains flat-burr grinders? If yes, how do you stop a 600 micron pour-over note from landing in the espresso band?",
        "Is dial 7.9 versus dial 8.9 a real disagreement, given that a tasted step on this grinder is about 2 to 3 clicks?",
        "What single measurement, on one locked recipe, would make you abandon the map you chose?",
    ]
    for i, q in enumerate(questions, 1):
        story.append(P(f"<b>{i}.</b>  {q}", "Q"))

    story.append(P("Sources checked 12 November 2026", "H"))
    sources = [
        "Homebrew Grind Convert, GetGrindConversionProfilesUseCase.kt. K-Ultra M = 100 × dial + 50. C40 M = 30 × clicks. Comment in the file: micron is estimated particle size, not burr-gap travel.",
        "1Zpresso K-Ultra grind-setting chart, image dated 31 Oct 2022, linked from 1zpresso.coffee/grind-setting/. Dial bands, no microns. Page text: 20 micron per click; 6.5 means 65 clicks.",
        "Coffee Notes grind table, coffeenotes.fyi/grind. Step treated as the micron unit, zero offset. K-Ultra 20 micron/click, C40 30 micron/click. Pour-over bar on their 200–1600 axis is 500–1200 micron.",
        "Coffee Notes, Devil's recipe, coffeenotes.fyi/recipe/v60-kasuya-improved-devils. ~840 micron, 28 C40 clicks.",
        "Coffee Notes, Archer fermented naturals, coffeenotes.fyi/recipe/v60-archers-fermented-naturals. 700–800 micron.",
        "Honest Coffee Guide, K-Ultra page. Header particle span 0–760 micron. V60 written as 0.5.3–0.9.2, which is dial 5.3–9.2. Wide, overlapping method rows.",
        "Cuppd grind converter, cuppdcoffee.com/grind. K-Ultra 0.0.0–1.0.0 = 0–760 micron. Published rows used above: 600 micron = 0.7.9; 650 = 0.8.6 and C40 24; 700 = 0.9.2 and C40 26. C40 0–40 clicks = 0–1090 micron on their scale, which is not 30 micron × clicks.",
        "CuppaData, cuppadata.com, K-Ultra versus C40, fitted from Honest charts. Same neighborhood as Cuppd (V60 about dial 7.4 and C40 21 clicks). Used only as a second Honest-family source.",
        "Beean converter, beeancoffee.com/grinder-setting-converter/. Observed: 600 micron → dial 6.3; C40 28 clicks → dial 8.9. Method page: crowdsourced, not a published formula. The micron numeral on the 28-click screen was excluded.",
        "Beean, beeancoffee.com/1zpresso-k-ultra-grind-settings/. V60 start dial 8.0, range 7.0–9.5. Espresso start 3.0, range 2.5–3.5. 20 micron is described as burr movement.",
        "NNSCoffee, nnscoffee.com/grind-setting-comparison/. Not one of the four maps. C40 24–28 → K-Ultra 7.0–8.0. C40 25 ≈ 7.2–7.5.",
    ]
    for s in sources:
        story.append(P("- " + s, "Small"))

    doc.build(story, onFirstPage=footer, onLaterPages=footer)
    print(OUT)


if __name__ == "__main__":
    build()
