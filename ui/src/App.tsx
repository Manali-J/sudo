import {useEffect, useState} from "react";
import {createInitialBoard} from "./features/sudoku/constants";
import {ControlPanel} from "./features/sudoku/components/ControlPanel";
import {SudokuBoard} from "./features/sudoku/components/SudokuBoard";
import {SudokuToolbar} from "./features/sudoku/components/SudokuToolbar";
import type {CellData, CellPosition, Difficulty, Mode} from "./features/sudoku/types";

function App() {
    const [cells, setCells] = useState<CellData[]>(() => createInitialBoard());
    const [selectedCell, setSelectedCell] = useState<CellPosition>({ row: 6, col: 2 });
    const [mode] = useState<Mode>("Guided");
    const [difficulty] = useState<Difficulty>("Medium");
    const [pencilMode, setPencilMode] = useState(false);
    const [highlightedDigit, setHighlightedDigit] = useState<number | null>(null);

    function updateSelectedCellValue(nextValue: number | null) {
        setCells((currentCells) =>
            currentCells.map((cell) => {
                if (cell.row !== selectedCell.row || cell.col !== selectedCell.col || cell.given) {
                    return cell;
                }

                return {
                    ...cell,
                    value: nextValue,
                    pencilMarks: nextValue === null ? cell.pencilMarks : []
                };
            })
        );
    }

    function toggleSelectedCellPencilMark(mark: number) {
        setCells((currentCells) =>
            currentCells.map((cell) => {
                if (cell.row !== selectedCell.row || cell.col !== selectedCell.col || cell.given) {
                    return cell;
                }

                if (cell.value !== null) {
                    return cell;
                }

                const hasMark = cell.pencilMarks.includes(mark);
                const nextMarks = hasMark
                    ? cell.pencilMarks.filter((value) => value !== mark)
                    : [...cell.pencilMarks, mark].sort((left, right) => left - right);

                return {
                    ...cell,
                    pencilMarks: nextMarks
                };
            })
        );
    }

    function clearSelectedCell() {
        setCells((currentCells) =>
            currentCells.map((cell) => {
                if (cell.row !== selectedCell.row || cell.col !== selectedCell.col || cell.given) {
                    return cell;
                }

                return {
                    ...cell,
                    value: null,
                    pencilMarks: []
                };
            })
        );
    }

    function handleDigitInput(digit: number) {
        setHighlightedDigit((currentDigit) => (currentDigit === digit ? null : digit));

        if (pencilMode) {
            toggleSelectedCellPencilMark(digit);
            return;
        }

        updateSelectedCellValue(digit);
    }

    function moveSelectedCell(rowDelta: number, colDelta: number) {
        setHighlightedDigit(null);
        setSelectedCell((currentCell) => ({
            row: Math.max(0, Math.min(8, currentCell.row + rowDelta)),
            col: Math.max(0, Math.min(8, currentCell.col + colDelta))
        }));
    }

    const disabledDigits = new Set<number>();
    for (let digit = 1; digit <= 9; digit += 1) {
        const count = cells.filter((cell) => cell.value === digit).length;
        if (count >= 9) {
            disabledDigits.add(digit);
        }
    }

    useEffect(() => {
        function handleKeyDown(event: KeyboardEvent) {
            const target = event.target;
            if (
                target instanceof HTMLInputElement ||
                target instanceof HTMLTextAreaElement ||
                target instanceof HTMLSelectElement
            ) {
                return;
            }

            if (event.key >= "1" && event.key <= "9") {
                event.preventDefault();
                handleDigitInput(Number(event.key));
                return;
            }

            if (event.key === "ArrowUp") {
                event.preventDefault();
                moveSelectedCell(-1, 0);
                return;
            }

            if (event.key === "ArrowDown") {
                event.preventDefault();
                moveSelectedCell(1, 0);
                return;
            }

            if (event.key === "ArrowLeft") {
                event.preventDefault();
                moveSelectedCell(0, -1);
                return;
            }

            if (event.key === "ArrowRight") {
                event.preventDefault();
                moveSelectedCell(0, 1);
                return;
            }

            if (event.key === "Backspace" || event.key === "Delete") {
                event.preventDefault();
                clearSelectedCell();
            }
        }

        window.addEventListener("keydown", handleKeyDown);
        return () => window.removeEventListener("keydown", handleKeyDown);
    }, [selectedCell, pencilMode]);

    return (
        <main className="page-shell">
            <section className="stage">
                <header className="hero-block">
                    <h1>Sudoku</h1>
                    <p>Fill the grid so each row, column, and 3x3 box contains digits 1-9</p>
                </header>

                <SudokuToolbar mode={mode} difficulty={difficulty} />

                <section className="play-area">
                    <SudokuBoard
                        cells={cells}
                        selectedCell={selectedCell}
                        highlightedDigit={highlightedDigit}
                        onSelectCell={(cell) => {
                            setHighlightedDigit(null);
                            setSelectedCell(cell);
                        }}
                    />

                    <ControlPanel
                        pencilMode={pencilMode}
                        disabledDigits={disabledDigits}
                        highlightedDigit={highlightedDigit}
                        onDigitInput={handleDigitInput}
                        onTogglePencilMode={() => setPencilMode((value) => !value)}
                        onErase={clearSelectedCell}
                    />
                </section>

                <section className="help-card">
                    <h2>How to Play</h2>
                    <ul>
                        <li>Click on a cell to select it.</li>
                        <li>Use number buttons to fill the selected cell.</li>
                        <li>Toggle pencil mode to sketch candidates before committing.</li>
                    </ul>
                </section>
            </section>
        </main>
    );
}

export default App;
