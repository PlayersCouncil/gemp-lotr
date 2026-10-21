package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_048_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leader", "10_48");
					put("leader2", "10_48");

					put("explorer", "4_250");   // Southron Explorer: a Southron Man, strength 5, vitality 3; a regroup ability only
					put("nomad", "7_132");      // Desert Nomad: a Southron Man; damage +1 only at 2 threats
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man who is no Southron (fierce only at 2 burdens)
					put("rearguard", "103_43"); // Bladetusk Rearguard: [raider] artifact that its maneuver ability makes a Southron minion who is no Man

					put("harad", "4_238");      // Men of Harad: a [raider] support-area condition, cost 0; a Shadow ability only
					put("tidings", "7_130");    // Dark Tidings: a [raider] support-area condition, cost 1; a regroup ability only
					put("spear", "4_255");      // Southron Spear: a [raider] possession, no condition
					put("ambition", "1_133");   // Saruman's Ambition: an [isengard] support-area condition, cost 0
					put("raiders", "4_242");    // Raiders From the East: a [raider] support-area condition, for the Free Peoples player's own discard pile

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SeasonedLeaderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Seasoned Leader
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Vitality: 1
		 * Game Text: Bearer must be a Southron.<br>Bearer is <b>enduring</b>.<br><b>Skirmish:</b> Remove a threat to play a [raider] condition from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("leader");

		assertEquals("Seasoned Leader", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnASouthronAndMakesHimEnduring() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leader = scn.GetShadowCard("leader");
		var explorer = scn.GetShadowCard("explorer");
		var nomad = scn.GetShadowCard("nomad");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(leader);
		assertInHand(leader);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, nomad, axeman);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(nomad, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(explorer, Keyword.ENDURING));
		assertTrue(scn.ShadowPlayAvailable(leader));
		scn.ShadowPlayCard(leader);
		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer, nomad));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axeman, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(nomad);

		assertAttachedTo(leader, nomad);
		assertTrue(scn.HasKeyword(nomad, Keyword.ENDURING));
		// The other Southron, who does not bear it, is not.
		assertFalse(scn.HasKeyword(explorer, Keyword.ENDURING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesAThreatToPlayARaiderConditionFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leader = scn.GetShadowCard("leader");
		var leader2 = scn.GetShadowCard("leader2");
		var explorer = scn.GetShadowCard("explorer");
		var nomad = scn.GetShadowCard("nomad");
		var axeman = scn.GetShadowCard("axeman");
		var rearguard = scn.GetShadowCard("rearguard");
		var harad = scn.GetShadowCard("harad");
		var spear = scn.GetShadowCard("spear");
		var ambition = scn.GetShadowCard("ambition");
		scn.MoveCardsToSupportArea(rearguard);
		scn.MoveCardsToDiscard(leader2, harad, spear, ambition);
		// The Free Peoples player's own [raider] condition, in his own discard pile: a title in none of the Shadow player's piles.
		var raiders = scn.GetFreepsCard("raiders");
		scn.MoveCardsToDiscard(raiders);
		assertInDiscard(leader2, harad, spear, ambition, raiders);
		assertInZone(Zone.SUPPORT, rearguard);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, nomad, axeman);
		scn.AttachCardsTo(explorer, leader);
		assertAttachedTo(leader, explorer);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(7);
		scn.FreepsPass();
		// The Rearguard is a Southron minion who is no Man from here to the end of the turn.
		scn.ShadowUseCardAction(rearguard);
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		assertFalse(scn.IsRace(rearguard, Race.MAN));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, explorer);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(leader));
		scn.ShadowUseCardAction(leader);
		assertEquals(1, scn.GetThreats());

		// [raider] conditions in his own discard pile; not a [raider] possession, a condition of another culture, or the other player's.
		assertTrue(scn.ShadowHasCardChoiceAvailable(harad, leader2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(spear, ambition, raiders));
		scn.ShadowChooseCardBPFromSelection(leader2);

		// The second Seasoned Leader goes on a Southron, a Man or not: the Explorer, who bears one already, among them.
		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer, nomad, rearguard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axeman, aragorn));
		scn.ShadowChooseCard(rearguard);

		assertAttachedTo(leader2, rearguard);
		assertTrue(scn.HasKeyword(rearguard, Keyword.ENDURING));
		assertInDiscard(harad, spear, ambition, raiders);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoThreatToRemoveTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leader = scn.GetShadowCard("leader");
		var explorer = scn.GetShadowCard("explorer");
		var harad = scn.GetShadowCard("harad");
		scn.MoveCardsToDiscard(harad);
		assertInDiscard(harad);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.AttachCardsTo(explorer, leader);
		assertAttachedTo(leader, explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, explorer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);
		assertEquals(0, scn.GetThreats());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(leader));
	}

	@Test
	public void WithNoRaiderConditionInTheDiscardPileTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leader = scn.GetShadowCard("leader");
		var explorer = scn.GetShadowCard("explorer");
		var spear = scn.GetShadowCard("spear");
		var ambition = scn.GetShadowCard("ambition");
		scn.MoveCardsToDiscard(spear, ambition);
		assertInDiscard(spear, ambition);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.AttachCardsTo(explorer, leader);
		assertAttachedTo(leader, explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, explorer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(leader));
	}

	@Test
	public void WithTooLittleTwilightForTheConditionTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leader = scn.GetShadowCard("leader");
		var explorer = scn.GetShadowCard("explorer");
		var tidings = scn.GetShadowCard("tidings");
		scn.MoveCardsToDiscard(tidings);
		assertInDiscard(tidings);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.AttachCardsTo(explorer, leader);
		assertAttachedTo(leader, explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, explorer);
		scn.FreepsResolveSkirmish(aragorn);
		// Dark Tidings costs 1.
		scn.SetTwilight(0);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(leader));
	}

	@Test
	public void TheAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leader = scn.GetShadowCard("leader");
		var explorer = scn.GetShadowCard("explorer");
		var harad = scn.GetShadowCard("harad");
		scn.MoveCardsToDiscard(harad);
		assertInDiscard(harad);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.AttachCardsTo(explorer, leader);
		assertAttachedTo(leader, explorer);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(3);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(leader));
	}
}
