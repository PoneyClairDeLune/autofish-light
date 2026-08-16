package troy.autofish.config;

import com.google.gson.annotations.Expose;

public class Config {
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

    public long getRecastDelay() {
        return recastDelay;
    }
    public long getRandomDelay(){
        return randomPercent;
    }
    public String getClearLagRegex() {
        return clearLagRegex;
    }

    public void setRecastDelay(long recastDelay) {
        this.recastDelay = recastDelay;
    }
    public void setRandomDelay(long randomPercent){
        this.randomPercent = randomPercent;
    }
    public void setClearLagRegex(String clearLagRegex) {
        this.clearLagRegex = clearLagRegex;
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
