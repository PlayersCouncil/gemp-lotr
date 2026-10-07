package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_048_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scoutA", "103_48");
					put("scoutB", "103_48");
					put("scoutC", "103_48");
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man who is no Southron; nothing more below 2 burdens
					put("soldier", "4_254");    // Southron Soldier: [raider] Southron; ambush (2) and no other text
					put("nomad", "7_132");      // Desert Nomad: [raider] Southron; nothing more below 2 threats
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, no text at all
					put("ships", "8_65");       // Ships of Great Draught: [raider] possession in the support area; abilities never used here
					put("helmsman", "103_46");  // Corsair Helmsman: [raider]; Response: exert or hinder it to prevent a [raider] card being hindered

					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: vitality 4
					put("enter1", "103_9");     // You Cannot Enter Here: Maneuver: exert your Wizard to hinder a Shadow card; exhaust him if it is a minion
					put("enter2", "103_9");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesertWindScoutStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Desert Wind Scout
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron.</b>  Tracker.  Ambush (2).
		 * 	When you play this minion, remove (3) or hinder this minion.
		 * 	Each time this minion is hindered, you may hinder or restore another [raider] minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scoutA");

		assertEquals("Desert Wind Scout", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertTrue(scn.HasKeyword(card, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.AMBUSH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedForTwoAndTwoRoamingThenRemovingThreeLeavesItUnhinderedAndSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scoutA = scn.GetShadowCard("scoutA");
		var scoutB = scn.GetShadowCard("scoutB");
		scn.MoveCardsToHand(scoutA);

		scn.StartGame();
		scn.MoveMinionsToTable(scoutB);
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 4 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.HAND, scoutA);
		assertTrue(scn.ShadowPlayAvailable(scoutA));

		scn.ShadowPlayCard(scoutA);

		// 7 - 2 to play - 2 roaming (a site 4 minion played at site 2) leaves exactly the 3 the first
		// alternative needs.
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, scoutA);
		assertTrue(scn.ShadowChoiceAvailable("Remove (3)"));
		assertTrue(scn.ShadowChoiceAvailable("Hinder"));

		scn.ShadowChoose("Remove (3)");

		// It was not hindered, so nothing is offered; and the copy already in play is asked nothing.
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.IsHindered(scoutA));
		assertFalse(scn.IsHindered(scoutB));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ChoosingToHinderRemovesNothingAndLetsItHinderAnotherRaiderMinionOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scoutA = scn.GetShadowCard("scoutA");
		var scoutB = scn.GetShadowCard("scoutB");
		var axeman = scn.GetShadowCard("axeman");
		var soldier = scn.GetShadowCard("soldier");
		var brigand = scn.GetShadowCard("brigand");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(scoutA);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.MoveMinionsToTable(scoutB, axeman, soldier, brigand);
		// A Man of another culture is made a Southron, to tell the culture from the keyword.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, brigand, null, Keyword.SOUTHRON));
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.HasKeyword(brigand, Keyword.SOUTHRON));

		scn.ShadowPlayCard(scoutA);

		// 10 - 2 to play - 2 roaming
		assertEquals(6, scn.GetTwilight());
		assertFalse(scn.IsHindered(scoutA));

		scn.ShadowChoose("Hinder");

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scoutA));
		scn.ShadowChooseOptionalTrigger(scoutA);

		// Nothing is hindered yet, so there is nothing to restore: a minion to hinder is chosen. A Southron,
		// an Easterling and another Scout are [raider] minions; the Dunlending Southron and the Ships are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, axeman, scoutB));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand, ships, scoutA));
		assertFalse(scn.IsHindered(axeman));
		scn.ShadowChooseCard(axeman);

		assertTrue(scn.IsHindered(axeman));
		assertTrue(scn.IsHindered(scoutA));
		assertFalse(scn.IsHindered(soldier));
		assertFalse(scn.IsHindered(scoutB));
		assertFalse(scn.IsHindered(brigand));
		assertFalse(scn.IsHindered(ships));
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ItMayRestoreAnotherHinderedRaiderMinionInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scoutA = scn.GetShadowCard("scoutA");
		var scoutB = scn.GetShadowCard("scoutB");
		var axeman = scn.GetShadowCard("axeman");
		var soldier = scn.GetShadowCard("soldier");
		var nomad = scn.GetShadowCard("nomad");
		var brigand = scn.GetShadowCard("brigand");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(scoutA);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.MoveMinionsToTable(scoutB, axeman, soldier, nomad, brigand);
		// The Nomad is left unhindered, so that hindering is open to the player as well.
		scn.HinderCard(scoutB, axeman, soldier, brigand, ships);
		scn.SetTwilight(7);
		scn.FreepsPass();

		scn.ShadowPlayCard(scoutA);
		scn.ShadowChoose("Hinder");

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scoutA));
		scn.ShadowChooseOptionalTrigger(scoutA);
		assertTrue(scn.ShadowChoiceAvailable("Hinder another"));
		assertTrue(scn.ShadowChoiceAvailable("Restore another"));
		scn.ShadowChoose("Restore another");

		// The hindered [raider] minions, and not the Scout itself, the unhindered Nomad, the hindered
		// Dunlending or the hindered Ships.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, axeman, scoutB));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(scoutA, nomad, brigand, ships));
		assertTrue(scn.IsHindered(axeman));
		scn.ShadowChooseCard(axeman);

		assertFalse(scn.IsHindered(axeman));
		assertTrue(scn.IsHindered(soldier));
		assertTrue(scn.IsHindered(scoutB));
		assertTrue(scn.IsHindered(brigand));
		assertTrue(scn.IsHindered(ships));
		assertFalse(scn.IsHindered(nomad));
		assertTrue(scn.IsHindered(scoutA));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOnlyTwoLeftInThePoolItIsHinderedWithoutAChoiceAndDecliningItsTriggerChangesNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scoutA = scn.GetShadowCard("scoutA");
		var axeman = scn.GetShadowCard("axeman");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(scoutA);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, soldier);
		scn.HinderCard(soldier);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// 3 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(6, scn.GetTwilight());

		scn.ShadowPlayCard(scoutA);

		// 6 - 2 to play - 2 roaming leaves 2, one short of what removing (3) needs: no choice is put.
		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.ShadowChoiceAvailable("Remove (3)"));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scoutA));

		scn.ShadowDeclineOptionalTrigger();

		assertTrue(scn.IsHindered(scoutA));
		assertFalse(scn.IsHindered(axeman));
		assertTrue(scn.IsHindered(soldier));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AFreePeoplesCardHinderingItSetsItOffAndHinderingAnotherRaiderMinionDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scoutA = scn.GetShadowCard("scoutA");
		var axeman = scn.GetShadowCard("axeman");
		var soldier = scn.GetShadowCard("soldier");
		var gandalf = scn.GetFreepsCard("gandalf");
		var enter1 = scn.GetFreepsCard("enter1");
		var enter2 = scn.GetFreepsCard("enter2");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(enter1, enter2);

		scn.StartGame();
		scn.MoveMinionsToTable(scoutA, axeman, soldier);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(enter1);
		scn.FreepsChooseCard(axeman);

		// Another [raider] minion is hindered beside the Scout: the Scout offers nothing.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.IsHindered(axeman));
		assertFalse(scn.IsHindered(scoutA));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// The event exhausted Gandalf for choosing a minion; he is healed so that he can pay for the second.
		scn.RemoveWoundsFromChar(gandalf, 3);
		scn.ShadowPass();
		scn.FreepsPlayCard(enter2);
		scn.FreepsChooseCard(scoutA);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scoutA));
		scn.ShadowChooseOptionalTrigger(scoutA);
		assertTrue(scn.ShadowChoiceAvailable("Hinder another"));
		assertTrue(scn.ShadowChoiceAvailable("Restore another"));
		// The Axeman is the only hindered [raider] minion there is to restore.
		scn.ShadowChoose("Restore another");

		assertFalse(scn.IsHindered(axeman));
		assertFalse(scn.IsHindered(soldier));
		assertTrue(scn.IsHindered(scoutA));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void EachTimeTheSameScoutIsHinderedItsTriggerIsOfferedAgainAndNotWhenAnotherScoutIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scoutA = scn.GetShadowCard("scoutA");
		var scoutB = scn.GetShadowCard("scoutB");
		var scoutC = scn.GetShadowCard("scoutC");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(scoutA, scoutB, scoutC);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SetTwilight(12);
		scn.FreepsPass();

		// 12 set + 1 for Frodo + 2 for the shadow number of site 2: three Scouts at 2 + 2 roaming each
		// leave 3, so that the third is still given its choice.
		assertEquals(15, scn.GetTwilight());

		// The first Scout is hindered for the first time, and its trigger is declined.
		scn.ShadowPlayCard(scoutA);
		scn.ShadowChoose("Hinder");
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scoutA));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.IsHindered(scoutA));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second Scout restores it.
		scn.ShadowPlayCard(scoutB);
		scn.ShadowChoose("Hinder");
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scoutB));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scoutA));
		scn.ShadowChooseOptionalTrigger(scoutB);
		scn.ShadowChoose("Restore another");
		assertFalse(scn.IsHindered(scoutA));
		assertTrue(scn.IsHindered(scoutB));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The third Scout is hindered while the first stands by unhindered: only the third offers.
		scn.ShadowPlayCard(scoutC);
		scn.ShadowChoose("Hinder");
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scoutC));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scoutA));
		scn.ShadowChooseOptionalTrigger(scoutC);
		scn.ShadowChoose("Hinder another");
		assertTrue(scn.ShadowHasCardChoiceAvailable(scoutA, axeman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(scoutB, scoutC));
		scn.ShadowChooseCard(scoutA);

		// The first Scout is hindered a second time, and offers again.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scoutA));
		scn.ShadowDeclineOptionalTrigger();

		assertTrue(scn.IsHindered(scoutA));
		assertTrue(scn.IsHindered(scoutB));
		assertTrue(scn.IsHindered(scoutC));
		assertFalse(scn.IsHindered(axeman));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Ignore("F-0120: the trigger is carried out when the Scout is about to be hindered, before it is: a Scout hindered by it finds the first Scout still unhindered, hinders it in turn, and the two answer each other without end")
	@Test
	public void AScoutHinderedByAnotherScoutsTriggerMayRestoreThatScout() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scoutA = scn.GetShadowCard("scoutA");
		var scoutB = scn.GetShadowCard("scoutB");
		scn.MoveCardsToHand(scoutA);

		scn.StartGame();
		scn.MoveMinionsToTable(scoutB);
		scn.SetTwilight(7);
		scn.FreepsPass();

		scn.ShadowPlayCard(scoutA);
		scn.ShadowChoose("Hinder");

		// The Scout is hindered, and that is what its trigger answers.
		assertTrue(scn.IsHindered(scoutA));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scoutA));
		scn.ShadowChooseOptionalTrigger(scoutA);

		// The other Scout is the only other [raider] minion, and it is not hindered: it is hindered now.
		assertTrue(scn.IsHindered(scoutB));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scoutB));
		scn.ShadowChooseOptionalTrigger(scoutB);

		// The first Scout is the only other [raider] minion, and it is hindered: it can only be restored.
		assertFalse(scn.IsHindered(scoutA));
		assertTrue(scn.IsHindered(scoutB));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Ignore("F-0120: the trigger is offered when the Scout is about to be hindered, beside the response that prevents the hindering, so it can be carried out for a Scout that is then never hindered")
	@Test
	public void ItsTriggerIsNotOnOfferUntilItIsHinderedAndNotAtAllWhenTheHinderingIsPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scoutA = scn.GetShadowCard("scoutA");
		var helmsman = scn.GetShadowCard("helmsman");
		var axeman = scn.GetShadowCard("axeman");
		var gandalf = scn.GetFreepsCard("gandalf");
		var enter = scn.GetFreepsCard("enter1");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(enter);

		scn.StartGame();
		scn.MoveMinionsToTable(scoutA, helmsman, axeman);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(enter);
		scn.FreepsChooseCard(scoutA);

		// The Scout is about to be hindered and is not yet: the Helmsman may answer, the Scout has nothing
		// to offer.
		assertFalse(scn.IsHindered(scoutA));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(helmsman));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scoutA));

		scn.ShadowChooseOptionalTrigger(helmsman);
		scn.ShadowChoose("Exert");

		assertEquals(1, scn.GetWoundsOn(helmsman));
		assertFalse(scn.IsHindered(scoutA));
		assertFalse(scn.IsHindered(axeman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoOtherRaiderMinionToHinderOrRestoreItsTriggerIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scoutA = scn.GetShadowCard("scoutA");
		var brigand = scn.GetShadowCard("brigand");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(scoutA);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		// An unhindered minion of another culture and a hindered [raider] possession: neither is a [raider]
		// minion to hinder or to restore.
		scn.MoveMinionsToTable(brigand);
		scn.HinderCard(ships);
		scn.SetTwilight(7);
		scn.FreepsPass();

		scn.ShadowPlayCard(scoutA);
		scn.ShadowChoose("Hinder");

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scoutA));
		assertTrue(scn.IsHindered(scoutA));
		assertFalse(scn.IsHindered(brigand));
		assertTrue(scn.IsHindered(ships));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
