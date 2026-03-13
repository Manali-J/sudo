import type { CellData, CellPosition } from "../types";

type SudokuBoardProps = {
    cells: CellData[];
    selectedCell: CellPosition;
    highlightedDigit: number | null;
    onSelectCell: (cell: CellPosition) => void;
};

export function SudokuBoard({ cells, selectedCell, highlightedDigit, onSelectCell }: SudokuBoardProps) {
    const selectedValue =
        cells.find((cell) => cell.row === selectedCell.row && cell.col === selectedCell.col)?.value ?? null;
    const activeHighlightDigit = highlightedDigit ?? selectedValue;
    const isNumberFocusMode = highlightedDigit !== null;

    return (
        <div className="board-frame" role="grid" aria-label="Sudoku board">
            <div className="board-grid">
                {cells.map((cell) => {
                    const isSelected =
                        !isNumberFocusMode &&
                        cell.row === selectedCell.row &&
                        cell.col === selectedCell.col;
                    const isPeer =
                        !isNumberFocusMode &&
                        (cell.row === selectedCell.row ||
                            cell.col === selectedCell.col ||
                            sameBox(cell, selectedCell));
                    const isSameValue =
                        activeHighlightDigit !== null &&
                        cell.value === activeHighlightDigit &&
                        !isSelected;

                    return (
                        <button
                            key={`${cell.row}-${cell.col}`}
                            type="button"
                            role="gridcell"
                            aria-selected={isSelected}
                            className={[
                                "cell",
                                cell.given ? "given" : "editable",
                                isSelected ? "selected" : "",
                                isSameValue ? "matching-value" : "",
                                !isSelected && isPeer ? "peer" : ""
                            ]
                                .filter(Boolean)
                                .join(" ")}
                            style={{
                                gridColumn: `${cell.col + Math.floor(cell.col / 3) + 1}`,
                                gridRow: `${cell.row + Math.floor(cell.row / 3) + 1}`
                            }}
                            onClick={() => onSelectCell({ row: cell.row, col: cell.col })}
                        >
                            {cell.value !== null ? (
                                cell.value
                            ) : cell.pencilMarks.length > 0 ? (
                                <span className="pencil-grid" aria-hidden="true">
                                    {Array.from({ length: 9 }, (_, index) => {
                                        const digit = index + 1;
                                        return (
                                            <span key={digit} className="pencil-mark">
                                                {cell.pencilMarks.includes(digit) ? digit : ""}
                                            </span>
                                        );
                                    })}
                                </span>
                            ) : null}
                        </button>
                    );
                })}
            </div>
        </div>
    );
}

function sameBox(cell: CellData, selectedCell: CellPosition) {
    return (
        Math.floor(cell.row / 3) === Math.floor(selectedCell.row / 3) &&
        Math.floor(cell.col / 3) === Math.floor(selectedCell.col / 3)
    );
}
