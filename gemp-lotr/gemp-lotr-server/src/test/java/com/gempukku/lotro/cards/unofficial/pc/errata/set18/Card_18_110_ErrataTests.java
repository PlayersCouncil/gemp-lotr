package com.gempukku.lotro.cards.unofficial.pc.errata.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_18_110_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lagan", "68_110");
					put("lagan2", "68_110");
					put("sam", "1_311");          // Sam, Son of Hamfast: [shire] Hobbit, vitality 4
					put("smeagol", "103_116");    // Smeagol, Once Like Us: [shire] companion with no race, vitality 4
					put("pippin", "103_31");      // Pippin, Trollslayer: [gondor] Hobbit, vitality 4
					put("maggot", "1_288");       // Farmer Maggot: [shire] Hobbit ally, vitality 3
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man
					put("gimli", "1_13");         // Gimli, Son of Gloin
					put("guard", "5_83");         // Household Guard: Valiant is its whole text
					put("defend", "4_115");       // Defend It and Hope: Maneuver event, exert a [gondor] Man to wound a minion

					put("rider", "12_161");       // Black Rider: [wraith] Nazgul, Fierce; stronger for each companion
					put("destroyer", "17_124");   // White Hand Destroyer: [uruk-hai] Uruk-hai, Fierce; hunter only with a site controlled
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, not fierce
					put("fighter1", "1_146");     // Uruk Fighter: "Maneuver: Spot 5 companions to make this minion fierce until the regroup phase."
					put("fighter2", "1_146");
					put("axeman1", "4_224");      // Easterling Axeman: strength +3 and fierce while 2 burdens can be spotted
					put("axeman2", "4_224");
					put("runner", "1_178");       // Goblin Runner: vitality 1
					put("beckoning", "12_83");    // The Beckoning Shadow: Shadow condition, support area; nothing below 5 burdens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PrizedLaganStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Prized Lagan
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: At the start of each turn, you may add (2) to heal a [shire] companion.
		 * 	Maneuver: Add a threat to spot a minion. That minion loses fierce and cannot gain fierce until the regroup
		 * 	phase.  Any Shadow player may remove (2) to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lagan");

		assertEquals("Prized Lagan", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtTheStartOfTheTurnItsOwnerMayAddTwoToHealAShireCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lagan = scn.GetFreepsCard("lagan");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var pippin = scn.GetFreepsCard("pippin");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(sam, smeagol, pippin);
		scn.MoveAlliesToTable(maggot);
		scn.MoveCardsToSupportArea(lagan);
		// Wounded: Frodo and Smeagol of the [shire] companions, a Hobbit of another culture, a [shire] ally.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(smeagol, 2);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(maggot, 1);

		scn.StartGame();
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(lagan));
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(smeagol));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, smeagol));
		// Sam has no wound to heal.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, pippin, maggot));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(smeagol);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(maggot));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoShireCompanionWoundedItIsStillOfferedAndTheTwilightIsAddedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lagan = scn.GetFreepsCard("lagan");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(sam, pippin);
		scn.MoveAlliesToTable(maggot);
		scn.MoveCardsToSupportArea(lagan);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(maggot, 1);

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(lagan));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(maggot));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachCopyMakesItsOwnOfferAndDecliningOneLeavesEverythingAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lagan = scn.GetFreepsCard("lagan");
		var lagan2 = scn.GetFreepsCard("lagan2");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(lagan, lagan2);
		scn.AddWoundsToChar(frodo, 2);

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(lagan));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(lagan2));
		assertEquals(0, scn.GetTwilight());

		// Frodo is the one [shire] companion with a wound.
		scn.FreepsChooseOptionalTrigger(lagan);
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(lagan));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(lagan2));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInZone(Zone.SUPPORT, lagan, lagan2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OfferedAgainAtTheStartOfItsOwnersNextTurnAndNotAtTheOtherPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lagan = scn.GetFreepsCard("lagan");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(lagan);
		scn.AddWoundsToChar(frodo, 3);

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(lagan));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(frodo));

		// The other player's turn: this player's Free Peoples cards are not active.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetTwilight());

		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(lagan));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverAbilityAddsAThreatAndASpottedMinionLosesFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lagan = scn.GetFreepsCard("lagan");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var rider = scn.GetShadowCard("rider");
		var destroyer = scn.GetShadowCard("destroyer");
		var savage = scn.GetShadowCard("savage");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveCardsToSupportArea(lagan, beckoning);
		scn.MoveMinionsToTable(rider, destroyer, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(lagan));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(destroyer, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		int twilight = scn.GetTwilight();

		scn.FreepsUseCardAction(lagan);
		assertEquals(1, scn.GetThreats());
		// Any minion, fierce or not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, destroyer, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(beckoning, frodo, sam));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(rider);
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();
		assertEquals(twilight, scn.GetTwilight());
		assertFalse(scn.HasKeyword(rider, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(destroyer, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Nobody is assigned.  The Destroyer brings the fierce assignment phase about, and the Black Rider is
		// still not fierce in it.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		assertFalse(scn.HasKeyword(rider, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(destroyer, Keyword.FIERCE));

		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
	}

	@Test
	public void TheShadowPlayerMayRemoveTwoToKeepTheMinionFierceAndTheThreatStaysAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lagan = scn.GetFreepsCard("lagan");
		var sam = scn.GetFreepsCard("sam");
		var rider = scn.GetShadowCard("rider");
		var destroyer = scn.GetShadowCard("destroyer");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveCardsToSupportArea(lagan);
		scn.MoveMinionsToTable(rider, destroyer);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetThreats());
		// 2 for Ettenmoors + 1 for each of 2 companions
		assertEquals(4, scn.GetTwilight());

		scn.FreepsUseCardAction(lagan);
		scn.FreepsChooseCard(rider);
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseYes();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(destroyer, Keyword.FIERCE));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOneTwilightTokenTheShadowPlayerIsNotAskedAndTheMinionLosesFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lagan = scn.GetFreepsCard("lagan");
		var sam = scn.GetFreepsCard("sam");
		var rider = scn.GetShadowCard("rider");
		var destroyer = scn.GetShadowCard("destroyer");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveCardsToSupportArea(lagan);
		scn.MoveMinionsToTable(rider, destroyer);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(1);

		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		scn.FreepsUseCardAction(lagan);
		scn.FreepsChooseCard(rider);
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.HasKeyword(rider, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(destroyer, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lagan = scn.GetFreepsCard("lagan");
		var sam = scn.GetFreepsCard("sam");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveCardsToSupportArea(lagan);
		scn.MoveMinionsToTable(savage);
		// Two companions: room for one more threat.
		scn.AddThreats(1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetThreatLimit());
		assertTrue(scn.FreepsActionAvailable(lagan));

		// The Savage is the one minion.  He is not fierce, and the Shadow player is asked all the same.
		scn.FreepsUseCardAction(lagan);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();

		scn.ShadowPass();
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertFalse(scn.FreepsActionAvailable(lagan));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoMinionToSpotItIsStillOfferedAndTheThreatIsAddedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lagan = scn.GetFreepsCard("lagan");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.MoveCardsToSupportArea(lagan);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// The one minion dies in the maneuver phase.
		scn.FreepsPlayCard(defend);
		assertInDiscard(runner);
		scn.ShadowPass();

		assertTrue(scn.FreepsActionAvailable(lagan));
		assertEquals(0, scn.GetThreats());
		int twilight = scn.GetTwilight();

		scn.FreepsUseCardAction(lagan);
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ASpottedMinionCannotBeMadeFierceByItsOwnAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lagan = scn.GetFreepsCard("lagan");
		var fighter1 = scn.GetShadowCard("fighter1");
		var fighter2 = scn.GetShadowCard("fighter2");
		// Five companions, which the Uruk Fighters' ability spots.
		scn.MoveCompanionsToTable("sam", "aragorn", "gimli", "guard");

		scn.StartGame();
		scn.MoveCardsToSupportArea(lagan);
		scn.MoveMinionsToTable(fighter1, fighter2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.HasKeyword(fighter1, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(fighter2, Keyword.FIERCE));

		scn.FreepsUseCardAction(lagan);
		scn.FreepsChooseCard(fighter1);
		// Not fierce now, but the effect will still happen: the Shadow player is asked.
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();
		assertFalse(scn.HasKeyword(fighter1, Keyword.FIERCE));

		assertTrue(scn.ShadowActionAvailable(fighter1));
		scn.ShadowUseCardAction(fighter1);
		assertFalse(scn.HasKeyword(fighter1, Keyword.FIERCE));

		// The other Uruk Fighter, not spotted, gains it on the same board.
		scn.FreepsPass();
		scn.ShadowUseCardAction(fighter2);
		assertTrue(scn.HasKeyword(fighter2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(fighter1, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ASpottedMinionCannotGainFierceFromItsOwnTextUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lagan = scn.GetFreepsCard("lagan");
		var sam = scn.GetFreepsCard("sam");
		var axeman1 = scn.GetShadowCard("axeman1");
		var axeman2 = scn.GetShadowCard("axeman2");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveCardsToSupportArea(lagan);
		scn.MoveMinionsToTable(axeman1, axeman2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.HasKeyword(axeman1, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(axeman2, Keyword.FIERCE));

		scn.FreepsUseCardAction(lagan);
		scn.FreepsChooseCard(axeman1);
		scn.ShadowChooseNo();

		// With 2 burdens an Easterling Axeman is fierce: the one spotted is not, the other is.
		scn.AddBurdens(2);
		assertFalse(scn.HasKeyword(axeman1, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(axeman2, Keyword.FIERCE));

		// Still so in the fierce assignment phase that the other Axeman brings about.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		assertFalse(scn.HasKeyword(axeman1, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(axeman2, Keyword.FIERCE));

		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.HasKeyword(axeman1, Keyword.FIERCE));
	}
}
