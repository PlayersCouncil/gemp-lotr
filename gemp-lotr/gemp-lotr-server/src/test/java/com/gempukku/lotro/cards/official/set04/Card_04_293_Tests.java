package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_293_Tests
{

	// The minions are played in the Shadow phase at site 2 of the Fellowship path, Ettenmoors 1_331.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("valleys", "4_293");

					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally and villager; a regroup ability used only on purpose
					put("deor", "7_222");       // Déor: [rohan] Man ally and villager; a skirmish ability used only on purpose
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ally who is no villager; an archery ability used only on purpose

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, cost 2, vitality 2, site 6 (roaming at site 2); text only on a weapon played on him
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, vitality 3, site 5 (roaming at site 2); damage +1 only
					put("goblinman", "2_42");   // Goblin Man: [isengard] Orc, cost 2, vitality 2, site 2 (not roaming there); a Shadow ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, cost 1, VITALITY 1, site 4 (roaming at site 2); adds (2) when played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ValleysoftheMarkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Valleys of the Mark
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 	Each time your opponent plays a roaming minion, you may spot a villager to exert that minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("valleys");

		assertEquals("Valleys of the Mark", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ARoamingMinionPlayedIsExertedAndAMinionThatIsNotRoamingIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valleys = scn.GetFreepsCard("valleys");
		var herdsman = scn.GetFreepsCard("herdsman");
		var goblinman = scn.GetShadowCard("goblinman");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(valleys);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(goblinman, chieftain);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Site 2: the Goblin Man (site 2) is not roaming, and nothing is offered.
		scn.ShadowPlayCard(goblinman);
		assertFalse(scn.HasKeyword(goblinman, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(goblinman));

		// The Chieftain (site 6) is roaming.
		scn.ShadowPlayCard(chieftain);
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(valleys));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(goblinman));
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferLeavesTheMinionUnexerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valleys = scn.GetFreepsCard("valleys");
		var herdsman = scn.GetFreepsCard("herdsman");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(valleys);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(chieftain);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(chieftain);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(valleys));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoVillagerToSpotNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valleys = scn.GetFreepsCard("valleys");
		var aldor = scn.GetFreepsCard("aldor");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(valleys);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToHand(chieftain);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Aldor is a [rohan] Man ally, and no villager.
		scn.ShadowPlayCard(chieftain);
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(chieftain));
	}

	@Test
	public void ARoamingMinionThatCannotExertIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valleys = scn.GetFreepsCard("valleys");
		var herdsman = scn.GetFreepsCard("herdsman");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(valleys);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		// The Runner is roaming but has vitality 1: the exertion would do nothing, and is not offered.
		scn.ShadowPlayCard(runner);
		assertTrue(scn.HasKeyword(runner, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(runner));
	}

	@Test
	public void EachRoamingMinionPlayedIsOfferedInTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valleys = scn.GetFreepsCard("valleys");
		var deor = scn.GetFreepsCard("deor");
		var chieftain = scn.GetShadowCard("chieftain");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(valleys);
		scn.MoveAlliesToTable(deor);
		scn.MoveCardsToHand(chieftain, savage);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Roaming minions of two cultures and races, each offered as it is played; Déor is the villager spotted.
		scn.ShadowPlayCard(chieftain);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(valleys));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(chieftain));

		scn.ShadowPlayCard(savage);
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(valleys));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(deor));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
