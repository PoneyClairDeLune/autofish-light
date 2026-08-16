package troy.autofish.modded;

/** <i>More (Fishing) Rod Variants</i> by <i>pnku</i>. */
public class MoreFishingRodVariants extends NamespacedContent {
	/** <i>More (Fishing) Rod Variants</i> by <i>pnku</i>. */
	public MoreFishingRodVariants() {
		super("mstv-mfrv", "mstv-mfrv", "More Fishing Rod Variants");
	}

	protected boolean populateIds() {
		if (rodIds.isEmpty()) {
			rodIds.add("mstv-mfrv:acacia_fishing_rod");
			rodIds.add("mstv-mfrv:bamboo_fishing_rod");
			rodIds.add("mstv-mfrv:birch_fishing_rod");
			rodIds.add("mstv-mfrv:cherry_fishing_rod");
			rodIds.add("mstv-mfrv:crimson_fishing_rod");
			rodIds.add("mstv-mfrv:dark_oak_fishing_rod");
			rodIds.add("mstv-mfrv:jungle_fishing_rod");
			rodIds.add("mstv-mfrv:mangrove_fishing_rod");
			rodIds.add("mstv-mfrv:pale_oak_fishing_rod");
			rodIds.add("mstv-mfrv:spruce_fishing_rod");
			rodIds.add("mstv-mfrv:warped_fishing_rod");
		}
		return true;
	}
}
