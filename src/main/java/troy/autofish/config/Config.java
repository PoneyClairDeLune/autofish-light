package troy.autofish.config;

import java.util.regex.Pattern;

import com.google.gson.annotations.Expose;

import troy.autofish.LogSession;

public class Config {
	private Pattern clearLagMatcherCompiled;
	private String clearLagRegexOld = "\\[ClearLag\\] Removed [0-9]+ Entities!";

	@Expose int damageSafeMargin = 1;
	@Expose boolean legacyPersistenceBreakMe = true; // PLACEHOLDER!
	@Expose boolean modEnabled = true;
	@Expose boolean multiRod = false;
	@Expose boolean noisyDetection = false;
	@Expose boolean openWaterDetected = true;
	@Expose boolean openWaterNewAlgo = true;
	@Expose boolean persistentMode = false;
	@Expose boolean rodBreakAvoided = true;
	@Expose boolean soundUsed = false;
	@Expose boolean unsafeFluids = false;
	@Expose long recastDelay = 1500;
	@Expose long randomPercent = 50;
	@Expose long reelInDelay = 1;
	@Expose String clearLagRegex = "\\[ClearLag\\] Removed [0-9]+ Entities!";

	/**
	* @return true if anything was changed
	*/
	public boolean enforceConstraints() {
		boolean changed = false;
		if (damageSafeMargin < 1) {
			damageSafeMargin = 1;
			changed = true;
		} else if (damageSafeMargin > 32) {
			damageSafeMargin = 32;
			changed = true;
		}
		if (recastDelay < 500) {
			recastDelay = 500;
			changed = true;
		}
		if (clearLagRegex == null) {
			clearLagRegex = "";
			changed = true;
		}
		return changed;
	}

	public int damageSafeMargin() {
		return damageSafeMargin;
	}
	public boolean legacyPersistence() {
		return legacyPersistenceBreakMe;
	}
	public boolean modEnabled() {
		return modEnabled;
	}
	public boolean multiRod() {
		return multiRod;
	}
	public boolean noisyDetection() {
		return noisyDetection;
	}
	public boolean openWaterDetected() {
		return openWaterDetected;
	}
	public boolean openWaterNewAlgo() {
		return openWaterNewAlgo;
	}
	public boolean persistentMode() {
		return persistentMode;
	}
	public boolean rodBreakAvoided() {
		return rodBreakAvoided;
	}
	public boolean soundUsed() {
		return soundUsed;
	}
	public boolean unsafeFluids() {
		return unsafeFluids;
	}
	public String clearLagRegexString() {
		return clearLagRegex;
	}

	public void damageSafeMargin(int value) {
		damageSafeMargin = value;
	}
	public void legacyPersistence(boolean value) {
		legacyPersistenceBreakMe = value;
	}
	public void modEnabled(boolean value) {
		modEnabled = value;
	}
	public void multiRod(boolean value) {
		multiRod = value;
	}
	public void noisyDetection(boolean value) {
		noisyDetection = value;
	}
	public void openWaterDetected(boolean value) {
		openWaterDetected = value;
	}
	public void openWaterNewAlgo(boolean value) {
		openWaterNewAlgo = value;
	}
	public void persistentMode(boolean value) {
		persistentMode = value;
	}
	public void rodBreakAvoided(boolean value) {
		rodBreakAvoided = value;
	}
	public void soundUsed(boolean value) {
		soundUsed = value;
	}
	public void unsafeFluids(boolean value) {
		unsafeFluids = value;
	}
	// Can I compile as soon as GSON populates it?
	public void clearLagRegexString(String value) {
		// TODO: Make the compilation and caching proper. The current approach is bloody awkward. Also make the pattern retriever methods attempt recompilation if the compiled pattern is not yet populated.
		String revertableOld = clearLagRegexOld;
		clearLagRegexOld = clearLagRegex;
		clearLagRegex = value;
		if (!compileRegex()) {
			clearLagRegexOld = revertableOld;
			clearLagRegex = clearLagRegexOld;
		};
	}

	/** Returns <code>true</code> when a new RegEx is compiled. */
	public boolean compileRegex() {
		if (clearLagRegex == null) return false;
		if (clearLagRegex.isBlank()) return true;
		if (clearLagRegexOld != null && clearLagRegex.equals(clearLagRegexOld)) return false;
		try {
			clearLagMatcherCompiled = Pattern.compile(clearLagRegex, Pattern.CASE_INSENSITIVE);
		} catch (Exception e) {
			LogSession.error("RegEx compile error:\n" + e.getMessage());
			return false;
		}
		return true;
	}
	public Pattern getClearLagPattern() {
		return clearLagMatcherCompiled;
	}
	public boolean matchClearLagPattern(String input) {
		if (input == null || clearLagRegex == null) return false;
		if (clearLagRegex.isBlank()) return false;
		if (clearLagMatcherCompiled == null) return false;
		return clearLagMatcherCompiled.matcher(input).find();
	}

    public long getRecastDelay() {
        return recastDelay;
    }
    public long getRandomDelay(){
        return randomPercent;
    }

    public void setRecastDelay(long recastDelay) {
        this.recastDelay = recastDelay;
    }
    public void setRandomDelay(long randomPercent){
        this.randomPercent = randomPercent;
    }

    public long getRandomPercent() {
        return randomPercent;
    }
    public void setRandomPercent(long randomPercent) {
        this.randomPercent = randomPercent;
    }

    public long getReelInDelay() {
        return reelInDelay;
    }
    public void setReelInDelay(long reelInDelay) {
        this.reelInDelay = reelInDelay;
    }
}
