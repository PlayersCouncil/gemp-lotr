package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_025_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The [esgaroth]
	// allies' home is site 6, Esgaroth 30_54, where they may be assigned like companions: those boards are reached by
	// SkipToSite(5) and the move.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alfrid", "32_25");

					// [esgaroth] Man allies, home 6H
					put("bain", "32_15");         // Baïn, Son of Bard: no archer; a start-of-turn trigger and a maneuver ability, not used
					put("master", "32_20");       // The Master, Master of Lake-Town: no archer; his start-of-turn trigger wants Bard
					put("bard", "30_22");         // Bard, The Bowman: archer; a regroup ability not used
					put("volunteers", "32_17");   // Esgaroth Volunteers: archer, vitality 4; a maneuver ability not used

					put("thorin", "30_19");       // Thorin, Oakenshield: [dwarven] companion, vitality 4; a fellowship ability not used
					put("balin", "30_2");         // Balin, Brother of Dwalin: [dwarven] companion, vitality 3; a fellowship ability not used
					put("gloin", "30_11");        // Glóin, Father of Gimli: [dwarven] companion, vitality 3; a skirmish ability not used
					put("dain", "32_2");          // Dain Ironfoot, Lord of the Iron Hills: [dwarven] ally, home 9H; his regroup trigger not reached
					// Crown of Erebor: its Assignment ability, with no Thorin in play, lets [dwarven] allies be assigned
					put("crown", "32_1");

					put("bats", "32_30");         // Dark Bats: [moria] Creature, strength 4; a Shadow ability not used

					// In the draw deck: Shadow conditions of two cultures, each cost 2 with no requirement to play, a
					// Shadow card that is no condition and a Free Peoples condition.
					put("jail", "32_39");         // Jail: [sauron] support-area condition; a regroup ability not used
					put("river", "31_50");        // Enchanted River: [spider] support-area condition; its trigger is for site 5
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, no text
					put("lord", "1_21");          // Lord of Moria: [dwarven] support-area condition
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void AlfridStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Alfrid, Lickspittle
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: Each time a [esgaroth] ally is assigned to a skirmish, exert a [dwarven] companion.
		 * 	<br><b>Maneuver:</b> Heal a [esgaroth] ally to play Alfrid from your discard pile.
		 * 	<br><b>Skirmish:</b> Exert Alfrid twice to play a Shadow condition from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("alfrid");

		assertEquals("Alfrid", card.getBlueprint().getTitle());
		assertEquals("Lickspittle", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachTimeAnEsgarothAllyIsAssignedTheShadowPlayerExertsADwarvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alfrid = scn.GetShadowCard("alfrid");
		var bats = scn.GetShadowCard("bats");
		var bain = scn.GetFreepsCard("bain");
		var master = scn.GetFreepsCard("master");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var gloin = scn.GetFreepsCard("gloin");
		var dain = scn.GetFreepsCard("dain");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin, gloin);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveAlliesToTable(bain, master, dain);
		// Glóin is exhausted (vitality 3, 2 wounds): he cannot exert.
		scn.AddWoundsToChar(gloin, 2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetWoundsOn(gloin));
		scn.FreepsPass();
		scn.MoveMinionsToTable(alfrid, bats);

		scn.SkipToAssignments();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(dain));

		// The Free Peoples player assigns Baïn.
		scn.FreepsAssignToMinions(bain, alfrid);
		assertTrue(scn.IsCharAssignedAgainst(bain, alfrid));
		// [dwarven] companions who can exert; not Bilbo, a [shire] companion, Dain, a [dwarven] ally, nor the
		// exhausted Glóin.
		assertTrue(scn.ShadowHasCardChoiceAvailable(thorin, balin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bilbo, dain, gloin));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(thorin);
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));

		// The Shadow player assigns the minion left over to The Master: the second assignment, the second exertion.
		scn.ShadowAssignToMinions(master, bats);
		assertTrue(scn.IsCharAssignedAgainst(master, bats));
		assertTrue(scn.ShadowHasCardChoiceAvailable(thorin, balin));
		scn.ShadowChooseCard(balin);
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(2, scn.GetWoundsOn(gloin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssigningACompanionOrADwarvenAllyExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alfrid = scn.GetShadowCard("alfrid");
		var bats = scn.GetShadowCard("bats");
		var balin = scn.GetFreepsCard("balin");
		var gloin = scn.GetFreepsCard("gloin");
		var dain = scn.GetFreepsCard("dain");
		var crown = scn.GetFreepsCard("crown");
		scn.MoveCompanionsToTable(balin, gloin);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(crown);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, crown);
		scn.FreepsPass();
		scn.MoveMinionsToTable(alfrid, bats);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.FreepsUseCardAction(crown);
		scn.PassCurrentPhaseActions();
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(gloin));
		assertEquals(0, scn.GetWoundsOn(dain));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{dain, bats}, new PhysicalCardImpl[]{balin, alfrid});
		assertTrue(scn.IsCharAssignedAgainst(dain, bats));
		assertTrue(scn.IsCharAssignedAgainst(balin, alfrid));
		// Neither is an [esgaroth] ally: no [dwarven] companion is asked for.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(gloin));
		assertEquals(0, scn.GetWoundsOn(dain));
	}

	@Test
	public void ManeuverActionHealsAWoundedEsgarothAllyToPlayAlfridFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alfrid = scn.GetShadowCard("alfrid");
		var bats = scn.GetShadowCard("bats");
		var bard = scn.GetFreepsCard("bard");
		var volunteers = scn.GetFreepsCard("volunteers");
		var bain = scn.GetFreepsCard("bain");
		var thorin = scn.GetFreepsCard("thorin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveAlliesToTable(bard, volunteers, bain, dain);
		scn.AddWoundsToChar(bard, 2);
		scn.AddWoundsToChar(volunteers, 1);
		scn.AddWoundsToChar(dain, 1);
		scn.AddWoundsToChar(thorin, 1);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetWoundsOn(bard));
		assertEquals(1, scn.GetWoundsOn(volunteers));
		assertEquals(0, scn.GetWoundsOn(bain));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetWoundsOn(thorin));
		scn.FreepsPass();
		scn.MoveMinionsToTable(bats);
		scn.MoveCardsToDiscard(alfrid);
		assertInZone(Zone.DISCARD, alfrid);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.DISCARD, alfrid);
		assertEquals(2, scn.GetWoundsOn(bard));
		assertEquals(1, scn.GetWoundsOn(volunteers));
		assertEquals(0, scn.GetWoundsOn(bain));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetWoundsOn(thorin));

		assertTrue(scn.ShadowActionAvailable(alfrid));
		scn.ShadowUseCardAction(alfrid);
		// Wounded [esgaroth] allies; not Baïn, who has no wound, nor Dain, a [dwarven] ally, nor Thorin, a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(bard, volunteers));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bain, dain, thorin));
		scn.ShadowChooseCard(bard);

		assertEquals(1, scn.GetWoundsOn(bard));
		assertEquals(1, scn.GetWoundsOn(volunteers));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertInZone(Zone.SHADOW_CHARACTERS, alfrid);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedWithNoWoundedEsgarothAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alfrid = scn.GetShadowCard("alfrid");
		var bats = scn.GetShadowCard("bats");
		var bard = scn.GetFreepsCard("bard");
		var thorin = scn.GetFreepsCard("thorin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveAlliesToTable(bard, dain);
		scn.AddWoundsToChar(dain, 1);
		scn.AddWoundsToChar(thorin, 1);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(bard));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetWoundsOn(thorin));
		scn.FreepsPass();
		scn.MoveMinionsToTable(bats);
		scn.MoveCardsToDiscard(alfrid);
		assertInZone(Zone.DISCARD, alfrid);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.DISCARD, alfrid);
		assertEquals(0, scn.GetWoundsOn(bard));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(alfrid));

		scn.AddWoundsToChar(bard, 1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(alfrid));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedWithOneTwilightTooFewToPlayAlfrid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alfrid = scn.GetShadowCard("alfrid");
		var bats = scn.GetShadowCard("bats");
		var bard = scn.GetFreepsCard("bard");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveAlliesToTable(bard);
		scn.AddWoundsToChar(bard, 1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetWoundsOn(bard));
		scn.FreepsPass();
		scn.MoveMinionsToTable(bats);
		scn.MoveCardsToDiscard(alfrid);
		assertInZone(Zone.DISCARD, alfrid);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// Alfrid costs 2 and, at site 6, is not roaming.
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.DISCARD, alfrid);
		assertEquals(1, scn.GetWoundsOn(bard));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(alfrid));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(alfrid));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void SkirmishActionExertsAlfridTwiceToPlayAShadowConditionFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alfrid = scn.GetShadowCard("alfrid");
		var jail = scn.GetShadowCard("jail");
		var river = scn.GetShadowCard("river");
		var troop = scn.GetShadowCard("troop");
		var lord = scn.GetShadowCard("lord");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(alfrid);
		assertInZone(Zone.DECK, jail, river, troop, lord);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, alfrid);
		scn.FreepsResolveSkirmish(thorin);
		scn.SetTwilight(10);
		scn.FreepsPass();

		int shuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(0, scn.GetWoundsOn(alfrid));
		assertTrue(scn.ShadowActionAvailable(alfrid));
		scn.ShadowUseCardAction(alfrid);
		assertEquals(2, scn.GetWoundsOn(alfrid));

		scn.DismissRevealedCards();
		// Shadow conditions of two cultures; not a Shadow card that is no condition, nor a Free Peoples condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(jail, river));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, lord));
		scn.ShadowChooseCardBPFromSelection(jail);

		assertInZone(Zone.SUPPORT, jail);
		assertInZone(Zone.DECK, river, troop, lord);
		// Shuffled as the card leaves the deck looked through, and again when the look is over; the other deck not.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWhileAlfridCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alfrid = scn.GetShadowCard("alfrid");
		var jail = scn.GetShadowCard("jail");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(alfrid);
		// Vitality 3 with 1 wound: he can exert once, not twice.
		scn.AddWoundsToChar(alfrid, 1);
		assertEquals(1, scn.GetWoundsOn(alfrid));
		assertInZone(Zone.DECK, jail);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, alfrid);
		scn.FreepsResolveSkirmish(thorin);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(alfrid));
	}

	@Test
	public void WithNoShadowConditionInTheDrawDeckTheSkirmishActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alfrid = scn.GetShadowCard("alfrid");
		var jail = scn.GetShadowCard("jail");
		var river = scn.GetShadowCard("river");
		var troop = scn.GetShadowCard("troop");
		var lord = scn.GetShadowCard("lord");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.MoveCardsToDiscard(jail, river);
		assertInZone(Zone.DISCARD, jail, river);
		scn.FreepsPass();
		scn.MoveMinionsToTable(alfrid);
		assertInZone(Zone.DISCARD, jail, river);
		assertInZone(Zone.DECK, troop, lord);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, alfrid);
		scn.FreepsResolveSkirmish(thorin);
		scn.SetTwilight(10);
		scn.FreepsPass();

		int shuffles = scn.GetShadowDeckShuffleCount();
		assertEquals(0, scn.GetWoundsOn(alfrid));
		// The Shadow conditions are in the discard pile, and the deck holds minions only.  A Hobbit-set play from the draw deck with nothing it could play is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418):
		// Alfrid does not exert twice for a look that finds nothing.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(alfrid));
		assertEquals(0, scn.GetWoundsOn(alfrid));
		assertInZone(Zone.DECK, troop, lord);
		assertInZone(Zone.DISCARD, jail, river);
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
	}
}
