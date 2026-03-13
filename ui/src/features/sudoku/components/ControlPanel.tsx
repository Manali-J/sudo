import { EraserIcon, LightbulbIcon, PencilIcon } from "./Icons";

type ControlPanelProps = {
    pencilMode: boolean;
    disabledDigits: Set<number>;
    highlightedDigit: number | null;
    onDigitInput: (digit: number) => void;
    onTogglePencilMode: () => void;
    onErase: () => void;
};

export function ControlPanel({
    pencilMode,
    disabledDigits,
    highlightedDigit,
    onDigitInput,
    onTogglePencilMode,
    onErase
}: ControlPanelProps) {
    const keypadHasFocus = highlightedDigit !== null;

    return (
        <aside className="control-card">
            <div className={`number-grid ${keypadHasFocus ? "has-active-digit" : ""}`}>
                {Array.from({ length: 9 }, (_, index) => (
                    <button
                        key={index + 1}
                        className={[
                            "pad-button",
                            highlightedDigit === index + 1 ? "active" : "",
                            keypadHasFocus && highlightedDigit !== index + 1 ? "inactive" : ""
                        ]
                            .filter(Boolean)
                            .join(" ")}
                        type="button"
                        disabled={disabledDigits.has(index + 1)}
                        onClick={() => onDigitInput(index + 1)}
                    >
                        {index + 1}
                    </button>
                ))}
            </div>

            <div className="dual-actions">
                <button
                    className={`action-button ${pencilMode ? "active" : ""}`}
                    type="button"
                    onClick={onTogglePencilMode}
                >
                    <PencilIcon />
                    <span>Notes {pencilMode ? "On" : "Off"}</span>
                </button>
                <button className="action-button" type="button" onClick={onErase}>
                    <EraserIcon />
                    <span>Erase</span>
                </button>
            </div>

            <button className="hint-button" type="button">
                <LightbulbIcon />
                <span className="hint-copy">
                    <span>Use Hint</span>
                    <span>3 remaining</span>
                </span>
            </button>
        </aside>
    );
}
