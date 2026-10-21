package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_089_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warg", "10_89");

					put("gothmog", "10_88");       // Gothmog, Lieutenant of Morgul: [sauron] Orc, strength 13; the only Gothmog who is a [sauron] Orc
					put("chieftain", "1_266");     // Orc Chieftain: [sauron] Orc, strength 7; answers only a weapon played on him
					put("hound", "7_192");         // Morgul Hound: [wraith] Orc, an Orc who is not [sauron]; +4 only at 2 wounds on the Ring-bearer
					put("guard", "10_81");         // Cirith Ungol Guard: [sauron] Uruk-hai, a [sauron] minion who is no Orc; Damage +1 only
					put("mordorpillager", "10_92"); // Mordor Pillager: "Shadow: Exert this minion and spot another [sauron] minion to control a site"

					put("aragorn", "1_89");        // Aragorn, Ranger of the North: [gondor]
					put("gimli", "1_13");          // Gimli, Son of Gloin: [dwarven]
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GothmogsWargStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Gothmog's Warg
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: +1
		 * Vitality: +1
		 * Game Text: Bearer must be a [sauron] Orc.<br>For each site you control, bearer is strength +1 (or +2 if bearer is Gothmog).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warg");

		assertEquals("Gothmog's Warg", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnlyOnASauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn");

		var warg = scn.GetShadowCard("warg");
		var gothmog = scn.GetShadowCard("gothmog");
		var chieftain = scn.GetShadowCard("chieftain");
		var hound = scn.GetShadowCard("hound");
		var guard = scn.GetShadowCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(warg);

		scn.StartGame();
		scn.MoveMinionsToTable(gothmog, chieftain, hound, guard);
		scn.SetTwilight(10);
		assertInHand(warg);
		scn.FreepsPass();

		scn.ShadowPlayCard(warg);

		assertTrue(scn.ShadowHasCardChoiceAvailable(gothmog, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, guard, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(chieftain);

		assertInZone(Zone.ATTACHED, warg);
		assertSame(chieftain, warg.getAttachedTo());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcBearerIsStrengthPlusOneForEachSiteItsOwnerControlsAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var gothmog = scn.GetShadowCard("gothmog");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(gothmog, chieftain);
		scn.AttachCardsTo(chieftain, warg);
		assertSame(chieftain, warg.getAttachedTo());

		// Player 1 took site 1 on player 2's turn: the Warg's owner controls nothing.
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		assertEquals(8, scn.GetStrength(chieftain));     // 7 + 1 printed
		assertEquals(13, scn.GetStrength(gothmog));

		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		// Gothmog is in play but does not bear it: +1, not +2, and Gothmog is untouched.
		assertEquals(9, scn.GetStrength(chieftain));     // 7 + 1 printed + 1 for a site
		assertEquals(13, scn.GetStrength(gothmog));

		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(3)));
		assertEquals(10, scn.GetStrength(chieftain));    // 7 + 1 printed + 2 for two sites
		assertEquals(13, scn.GetStrength(gothmog));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BorneByGothmogItIsStrengthPlusTwoForEachSiteAsASiteIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Three Free Peoples cultures ([shire], [gondor], [dwarven]): the Mordor Pillager's site is not preventable.
		scn.MoveCompanionsToTable("aragorn", "gimli");

		var warg = scn.GetShadowCard("warg");
		var gothmog = scn.GetShadowCard("gothmog");
		var mordorpillager = scn.GetShadowCard("mordorpillager");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		scn.MoveMinionsToTable(gothmog, mordorpillager);
		scn.AttachCardsTo(gothmog, warg);
		assertSame(gothmog, warg.getAttachedTo());
		scn.FreepsPass();

		assertEquals(16, scn.GetStrength(gothmog));      // 13 + 1 printed + 2 for one site
		assertEquals(9, scn.GetStrength(mordorpillager));
		assertEquals(0, scn.GetWoundsOn(mordorpillager));

		scn.ShadowUseCardAction(mordorpillager);

		assertEquals(1, scn.GetWoundsOn(mordorpillager));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		assertEquals(18, scn.GetStrength(gothmog));      // 13 + 1 printed + 4 for two sites
		assertEquals(9, scn.GetStrength(mordorpillager));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
