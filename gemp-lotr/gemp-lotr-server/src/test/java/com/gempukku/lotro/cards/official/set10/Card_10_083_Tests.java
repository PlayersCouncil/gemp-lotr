package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_083_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sentinel", "10_83");
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, cost 3, damage +1 and no other text
					put("shagrat", "10_99");    // Shagrat, Captain of Cirith Ungol: [sauron] Uruk-hai of another title; text only on a skirmish lost
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, cost 2, no text; held in hand beside this card
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc, no Uruk-hai; text only on a weapon PLAYED on him
					put("pursuer", "4_190");    // Uruk Pursuer: an [isengard] Uruk-hai; its text reaches no unbound Hobbit here
					put("banner", "6_98");      // Banner of the Eye: [sauron] possession on a [sauron] Orc; text only on a skirmish won

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; skirmish ability only
					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession on a Dwarf; changes no number
					put("ring", "17_4");        // Ring of Artifice: [dwarven] ARTIFACT on a Dwarf, no possession
					put("toby", "1_305");       // Old Toby: [shire] possession in the support area; its draw only on its own play
					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] condition in the support area, no possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CirithUngolSentinelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Cirith Ungol Sentinel
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Damage +1</b>.<br>If you can spot another [sauron] Uruk-hai, this minion is twilight cost -1 for each possession you can spot.
		*/

		var scn = GetScenario();

		var sentinel = scn.GetFreepsCard("sentinel");

		assertEquals("Cirith Ungol Sentinel", sentinel.getBlueprint().getTitle());
		assertNull(sentinel.getBlueprint().getSubtitle());
		assertFalse(sentinel.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, sentinel.getBlueprint().getSide());
		assertEquals(Culture.SAURON, sentinel.getBlueprint().getCulture());
		assertEquals(CardType.MINION, sentinel.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, sentinel.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(sentinel, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sentinel, Keyword.DAMAGE));
		assertEquals(2, sentinel.getBlueprint().getTwilightCost());
		assertEquals(7, sentinel.getBlueprint().getStrength());
		assertEquals(2, sentinel.getBlueprint().getVitality());
		assertEquals(6, sentinel.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithNoOtherSauronUrukHaiThereIsNoDiscountUntilOneIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var ring = scn.GetFreepsCard("ring");
		var toby = scn.GetFreepsCard("toby");
		var stairs = scn.GetFreepsCard("stairs");
		var sentinel = scn.GetShadowCard("sentinel");
		var sentry = scn.GetShadowCard("sentry");
		var guard = scn.GetShadowCard("guard");
		var chieftain = scn.GetShadowCard("chieftain");
		var pursuer = scn.GetShadowCard("pursuer");
		var banner = scn.GetShadowCard("banner");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, ring);
		scn.MoveCardsToSupportArea(toby, stairs);
		scn.MoveCardsToHand(sentinel, sentry, guard);
		assertAttachedTo(ring, gimli);
		assertInZone(Zone.SUPPORT, toby);
		assertInZone(Zone.SUPPORT, stairs);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, pursuer);
		scn.AttachCardsTo(chieftain, banner);
		assertAttachedTo(banner, chieftain);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Two possessions (Old Toby, a Free Peoples one in a support area; the Banner, a Shadow one a minion bears),
		// but no other [sauron] Uruk-hai: a [sauron] Orc and an [isengard] Uruk-hai are none.
		assertEquals(2, scn.GetTwilightCost(sentinel));
		assertEquals(2, scn.GetTwilightCost(sentry));

		scn.ShadowPlayCard(guard);
		assertInZone(Zone.SHADOW_CHARACTERS, guard);

		// Now -1 for each of the two possessions; the artifact and the condition are none.  The other minion in
		// hand is not discounted.
		assertEquals(0, scn.GetTwilightCost(sentinel));
		assertEquals(2, scn.GetTwilightCost(sentry));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsTwilightCostMinus1ForEachPossessionWhileAnotherSauronUrukHaiIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var ring = scn.GetFreepsCard("ring");
		var toby = scn.GetFreepsCard("toby");
		var stairs = scn.GetFreepsCard("stairs");
		var sentinel = scn.GetShadowCard("sentinel");
		var sentry = scn.GetShadowCard("sentry");
		var shagrat = scn.GetShadowCard("shagrat");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor, ring);
		scn.MoveCardsToSupportArea(stairs);
		scn.MoveCardsToHand(sentinel, sentry, toby);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(ring, gimli);
		assertInZone(Zone.SUPPORT, stairs);

		scn.StartGame();
		scn.MoveMinionsToTable(shagrat);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// One possession, borne by a companion; beside it an artifact and a condition.
		assertEquals(1, scn.GetTwilightCost(sentinel));
		assertEquals(2, scn.GetTwilightCost(sentry));

		// A second possession.
		scn.MoveCardsToSupportArea(toby);
		assertInZone(Zone.SUPPORT, toby);
		assertEquals(0, scn.GetTwilightCost(sentinel));
		assertEquals(2, scn.GetTwilightCost(sentry));

		// Back to one, and the card played at that cost.
		scn.MoveCardsToHand(toby);
		assertEquals(1, scn.GetTwilightCost(sentinel));
		int pool = scn.GetTwilight();
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(sentinel);
		assertInZone(Zone.SHADOW_CHARACTERS, sentinel);
		// 2 printed - 1 for one possession + 2 roaming (site 6 minion at site 2)
		assertEquals(pool - 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
