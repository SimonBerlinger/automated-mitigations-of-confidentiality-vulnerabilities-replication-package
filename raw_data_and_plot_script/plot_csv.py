#!/usr/bin/env python3
"""Plot a two-column CSV as a PDF line chart.

    python3 plot_csv.py data.csv
"""

import csv
import sys
from pathlib import Path

import matplotlib.pyplot as plt


def main() -> None:
    if len(sys.argv) != 2:
        sys.exit("Usage: python3 plot_csv.py file.csv")

    path = Path(sys.argv[1])
    with path.open(newline="", encoding="utf-8-sig") as handle:
        rows = [row for row in csv.reader(handle) if any(cell.strip() for cell in row)]
    x_name, y_name = (cell.strip() for cell in rows[0])
    points = sorted((float(row[0]), float(row[1])) for row in rows[1:])
    xs, ys = zip(*points)

    plt.rcParams.update(
        {
            "font.family": "Inter",
            "font.size": 12,
            "axes.labelcolor": "#000000",
            "xtick.color": "#000000",
            "ytick.color": "#000000",
            "axes.spines.top": False,
            "axes.spines.right": False,
            "axes.spines.left": False,
            "axes.edgecolor": "#000000",
            "figure.facecolor": "white",
            "axes.facecolor": "white",
            "savefig.facecolor": "white",
            "pdf.fonttype": 42,
        }
    )
    figure, ax = plt.subplots(figsize=(10, 5.6))
    ax.plot(xs, ys, color="#00008B", linewidth=2.4, marker="o", markersize=7, markerfacecolor="white", markeredgewidth=1.8)
    ax.set_xlabel(x_name)
    ax.set_ylabel(y_name)
    ax.grid(axis="y", color="#999999")
    ax.tick_params(length=0)
    figure.tight_layout()
    figure.savefig(path.with_name(f"{path.stem}_plot.pdf"))


if __name__ == "__main__":
    main()
