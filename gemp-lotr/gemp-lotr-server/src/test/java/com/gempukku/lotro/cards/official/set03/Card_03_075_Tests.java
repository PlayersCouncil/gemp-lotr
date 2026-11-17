package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_075_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ravager1", "3_75");
					put("ravager2", "3_75");
					put("keeper", "3_68");      // Saruman, Keeper of Isengard: [isengard] Wizard minion; makes Uruk-hai fierce, nothing that reads here
					put("master", "17_116");    // Saruman, Master of the White Hand: [uruk-hai] Wizard minion; each companion resistance -1
					put("servant", "17_38");    // Saruman, Servant of Sauron: [isengard] follower, in the support area until its Aid is paid
					put("white", "32_24");      // Saruman, The White: [gandalf] Wizard ally; his abilities are used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a Wizard who is no Saruman
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai who is no Saruman; damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukRavagerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Uruk Ravager
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>When you play this minion, you may spot Saruman to add (2).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ravager1");

		assertEquals("Uruk Ravager", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedWithAnIsengardSarumanInPlayItMayAddTwoAndOnlyTheCopyPlayedAsks() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ravager1 = scn.GetShadowCard("ravager1");
		var ravager2 = scn.GetShadowCard("ravager2");
		var keeper = scn.GetShadowCard("keeper");
		scn.MoveCardsToHand(ravager1, ravager2);

		scn.StartGame();
		scn.MoveMinionsToTable(keeper);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(ravager1);

		// The play is paid for before the offer: the pool is read there, and the offer adds 2 to it.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ravager1));
		int pool = scn.GetTwilight();
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second copy played: the one already in play does not answer it, the new one asks, and declined adds nothing.
		scn.ShadowPlayCard(ravager2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ravager2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(ravager1));
		pool = scn.GetTwilight();
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(pool, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, ravager1, ravager2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoSarumanInPlayItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ravager = scn.GetShadowCard("ravager1");
		var keeper = scn.GetShadowCard("keeper");
		var savage = scn.GetShadowCard("savage");
		var gandalf = scn.GetFreepsCard("gandalf");
		// Saruman in hand is not in play; Gandalf is a Wizard and the Savage an [isengard] Uruk-hai, neither a Saruman.
		scn.MoveCardsToHand(ravager, keeper);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(ravager);
		assertInZone(Zone.SHADOW_CHARACTERS, ravager);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(ravager));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASarumanOfAnotherCultureIsSarumanToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ravager = scn.GetShadowCard("ravager1");
		var master = scn.GetShadowCard("master");
		scn.MoveCardsToHand(ravager);

		scn.StartGame();
		scn.MoveMinionsToTable(master);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(ravager);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ravager));
		int pool = scn.GetTwilight();
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SarumanTheFollowerIsSarumanToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ravager = scn.GetShadowCard("ravager1");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(ravager);

		scn.StartGame();
		scn.MoveCardsToSupportArea(servant);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(ravager);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ravager));
		int pool = scn.GetTwilight();
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayersSarumanIsSarumanToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ravager = scn.GetShadowCard("ravager1");
		var white = scn.GetFreepsCard("white");
		scn.MoveCardsToHand(ravager);
		scn.MoveAlliesToTable(white);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		// "Spot Saruman" has no "your": the other player's active Saruman is spotted.
		scn.ShadowPlayCard(ravager);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ravager));
		int pool = scn.GetTwilight();
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
