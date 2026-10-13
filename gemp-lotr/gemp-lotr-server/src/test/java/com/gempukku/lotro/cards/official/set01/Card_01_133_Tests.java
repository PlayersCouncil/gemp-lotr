package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_133_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ambition", "1_133");
					put("ambition2", "1_133");

					put("hunt", "1_126");       // Hunt Them Down!: [isengard] Maneuver event, cost 3; makes an Uruk-hai fierce until the regroup phase
					put("fumes", "6_62");       // Fires and Foul Fumes: [isengard] Regroup event, cost 4 (only read in hand)
					put("gamble", "3_76");      // Dangerous Gamble: a [moria] Shadow event, cost 2 (only read in hand)

					put("raiding", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, cost 4, strength 9; damage +1 only
					put("tracking", "15_171");  // Tracking Uruk: [uruk-hai] Uruk-hai, strength 14; hunter 1 only (+1 while skirmishing a non-hunter)
					put("warrior", "4_165");    // Orthanc Warrior: an [isengard] Uruk-hai, strength 7; damage +1 only
					put("servant", "3_58");     // Isengard Servant: an [isengard] Orc who is no Uruk-hai, strength 7; a regroup ability used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumansAmbitionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Saruman's Ambition
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * The twilight cost of your [isengard] events is -1.
		 * <b>Skirmish:</b> Discard this condition to make an Uruk-hai strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ambition");

		assertEquals("Saruman's Ambition", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void YourIsengardEventsCostOneLessAndNoOtherCardDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ambition = scn.GetShadowCard("ambition");
		var hunt = scn.GetShadowCard("hunt");
		var fumes = scn.GetShadowCard("fumes");
		var gamble = scn.GetShadowCard("gamble");
		var raiding = scn.GetShadowCard("raiding");
		var tracking = scn.GetShadowCard("tracking");
		// The Free Peoples player's own copy of an [isengard] event: not "your" event to the condition's owner.
		var freepsHunt = scn.GetFreepsCard("hunt");

		scn.StartGame();
		scn.MoveCardsToHand(ambition, hunt, fumes, gamble, raiding);
		scn.MoveCardsToHand(freepsHunt);
		scn.MoveMinionsToTable(tracking);
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		assertEquals(3, scn.GetTwilightCost(hunt));
		assertEquals(4, scn.GetTwilightCost(fumes));
		assertEquals(4, scn.GetTwilightCost(raiding));
		assertEquals(2, scn.GetTwilightCost(gamble));
		assertEquals(3, scn.GetTwilightCost(freepsHunt));

		scn.ShadowPlayCard(ambition);
		assertInZone(Zone.SUPPORT, ambition);

		// [isengard] events of two phases are -1; an [isengard] minion, a [moria] event and the other
		// player's [isengard] event are not.
		assertEquals(2, scn.GetTwilightCost(hunt));
		assertEquals(3, scn.GetTwilightCost(fumes));
		assertEquals(4, scn.GetTwilightCost(raiding));
		assertEquals(2, scn.GetTwilightCost(gamble));
		assertEquals(3, scn.GetTwilightCost(freepsHunt));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		int pool = scn.GetTwilight();
		assertFalse(scn.HasKeyword(tracking, Keyword.FIERCE));

		// Tracking Uruk is the only Uruk-hai, so he is made fierce unasked.
		scn.ShadowPlayCard(hunt);
		// 3 printed - 1
		assertEquals(pool - 2, scn.GetTwilight());
		assertInDiscard(hunt);
		assertTrue(scn.HasKeyword(tracking, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void SkirmishActionDiscardsThisCopyToMakeAnUrukHaiOfAnyCultureStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ambition = scn.GetShadowCard("ambition");
		var ambition2 = scn.GetShadowCard("ambition2");
		var hunt = scn.GetShadowCard("hunt");
		var raiding = scn.GetShadowCard("raiding");
		var tracking = scn.GetShadowCard("tracking");
		var warrior = scn.GetShadowCard("warrior");
		var servant = scn.GetShadowCard("servant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(ambition, ambition2);

		scn.StartGame();
		scn.MoveCardsToHand(hunt);
		scn.MoveMinionsToTable(raiding, tracking, warrior, servant);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, raiding}, new PhysicalCardImpl[]{frodo, tracking});
		// The Orthanc Warrior and the Isengard Servant are left over, and stay unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, raiding));
		assertEquals(9, scn.GetStrength(raiding));
		assertEquals(14, scn.GetStrength(tracking));
		assertEquals(7, scn.GetStrength(warrior));
		assertEquals(7, scn.GetStrength(servant));
		// Both copies discount an [isengard] event: 3 - 1 - 1
		assertEquals(1, scn.GetTwilightCost(hunt));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(ambition));
		scn.ShadowUseCardAction(ambition);
		// Uruk-hai of two cultures, in this skirmish, in another and in none; not an [isengard] Orc, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiding, tracking, warrior));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(servant, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(raiding);

		assertEquals(11, scn.GetStrength(raiding));
		assertEquals(14, scn.GetStrength(tracking));
		assertEquals(7, scn.GetStrength(warrior));
		assertEquals(7, scn.GetStrength(servant));
		// The copy used is discarded and the other stays, its discount with it.
		assertInDiscard(ambition);
		assertInZone(Zone.SUPPORT, ambition2);
		assertEquals(2, scn.GetTwilightCost(hunt));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheStrengthBonusLastsToTheEndOfTheSkirmishPhaseAndNotIntoTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ambition = scn.GetShadowCard("ambition");
		var raiding = scn.GetShadowCard("raiding");
		var tracking = scn.GetShadowCard("tracking");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(ambition);

		scn.StartGame();
		scn.MoveMinionsToTable(raiding, tracking);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, tracking}, new PhysicalCardImpl[]{frodo, raiding});
		scn.FreepsResolveSkirmish(aragorn);

		// The Raiding Party waits for his own skirmish, the second of the turn.
		assertEquals(9, scn.GetStrength(raiding));
		scn.FreepsPass();
		scn.ShadowUseCardAction(ambition);
		scn.ShadowChooseCard(raiding);
		assertEquals(11, scn.GetStrength(raiding));

		// Still in force at the last decision of the skirmish phase it was made in.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(11, scn.GetStrength(raiding));

		// Gone when the next skirmish phase begins, and in his own skirmish.
		scn.ShadowPass();
		assertEquals(9, scn.GetStrength(raiding));
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, raiding));
		assertEquals(9, scn.GetStrength(raiding));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void InTheAssignmentPhaseTheSkirmishActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ambition = scn.GetShadowCard("ambition");
		var raiding = scn.GetShadowCard("raiding");
		scn.MoveCardsToSupportArea(ambition);

		scn.StartGame();
		scn.MoveMinionsToTable(raiding);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ambition));
	}
}
