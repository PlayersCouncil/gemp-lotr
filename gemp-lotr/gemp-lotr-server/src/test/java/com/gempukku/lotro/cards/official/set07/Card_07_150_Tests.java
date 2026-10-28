package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_150_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tongues", "7_150");
					put("tongues2", "7_150");
					put("explorer", "4_250");   // Southron Explorer: a [raider] Southron Man; a regroup ability used only on purpose
					// Easterling Axeman: a [raider] Easterling Man; strength +3 and fierce only at 2 burdens
					put("axeman", "4_224");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul and no Man; his text needs 3 burdens
					put("khamul", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a Man who is not [raider]; no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man of the Free Peoples player's
					// Five cards for the Free Peoples hand, so that he would have initiative: Goblin Patrol Troop, no text.
					put("troop1", "1_177");
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("troop4", "1_177");
					put("troop5", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HarshTonguesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Harsh Tongues
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a [raider] Man.<br><b>Regroup:</b> Remove (3) and spot a [raider] Man to place a [raider] token here.<br>While there are 3 [raider] tokens here, the Shadow has initiative regardless of the Free Peoples player's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tongues");

		assertEquals("Harsh Tongues", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedToTheSupportAreaWithASouthronToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tongues = scn.GetShadowCard("tongues");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToHand(tongues);
		assertInZone(Zone.HAND, tongues);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(tongues));
		scn.ShadowPlayCard(tongues);
		assertInZone(Zone.SUPPORT, tongues);
		assertNull(tongues.getAttachedTo());
		assertEquals(0, scn.GetCultureTokensOn(tongues));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilARaiderManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var tongues = scn.GetShadowCard("tongues");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(tongues, axeman);
		assertInZone(Zone.HAND, tongues, axeman);

		scn.StartGame();
		// A [raider] minion who is no Man, a Man who is not [raider], and the Free Peoples player's own Man.
		scn.MoveMinionsToTable(khamul, brigand);
		// With (1) left over once the Axeman is paid for: 2 set + 2 companions + 2 for the site, less 2 + 2 roaming.
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(tongues));

		// An Easterling is a [raider] Man.
		scn.ShadowPlayCard(axeman);
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertTrue(scn.ShadowPlayAvailable(tongues));

		scn.ShadowPlayCard(tongues);
		assertInZone(Zone.SUPPORT, tongues);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionRemoves3ToPlaceARaiderTokenOnThatCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tongues = scn.GetShadowCard("tongues");
		var tongues2 = scn.GetShadowCard("tongues2");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(tongues, tongues2);
		assertEquals(0, scn.GetCultureTokensOn(tongues));
		assertEquals(0, scn.GetCultureTokensOn(tongues2));

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(tongues));
		scn.ShadowUseCardAction(tongues);
		assertEquals(1, scn.GetTwilight()); // 4 - 3
		// The token goes on the copy used, and not on the other.
		assertEquals(1, scn.GetCultureTokensOn(tongues));
		assertEquals(0, scn.GetCultureTokensOn(tongues2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWithNoRaiderManToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var tongues = scn.GetShadowCard("tongues");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(tongues);
		assertInZone(Zone.SUPPORT, tongues);

		scn.StartGame();
		// A [raider] minion who is no Man, a Man who is not [raider], and the Free Peoples player's own Man.
		scn.MoveMinionsToTable(khamul, brigand);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		// Khamul is fierce: a fierce assignment phase, with nobody assigned again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(tongues));
		assertEquals(0, scn.GetCultureTokensOn(tongues));
	}

	@Test
	public void RegroupActionIsNotOfferedWithOnly2ToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tongues = scn.GetShadowCard("tongues");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToSupportArea(tongues);
		assertInZone(Zone.SUPPORT, tongues);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(tongues));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(tongues));
		assertEquals(0, scn.GetCultureTokensOn(tongues));
	}

	@Test
	public void TheThirdTokenPlacedGivesTheShadowInitiativeWhateverTheFreePeoplesHandAndAFourthKeepsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCardsToHand(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"),
				scn.GetFreepsCard("troop4"), scn.GetFreepsCard("troop5"));

		var tongues = scn.GetShadowCard("tongues");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(tongues);

		scn.StartGame();
		scn.AddTokensToCard(tongues, 2);
		scn.MoveMinionsToTable(axeman);
		assertEquals(2, scn.GetCultureTokensOn(tongues));
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(6);
		scn.FreepsPass();

		// Two tokens: the five cards in hand give the Free Peoples player initiative.
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		scn.ShadowUseCardAction(tongues);
		assertEquals(3, scn.GetCultureTokensOn(tongues));
		assertEquals(5, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());

		// A fourth token: still at least three, and still the Shadow's initiative.
		scn.FreepsPass();
		scn.ShadowUseCardAction(tongues);
		assertEquals(4, scn.GetCultureTokensOn(tongues));
		assertEquals(0, scn.GetTwilight()); // 6 - 3 - 3
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TwoTokensOnEachOfTwoCopiesDoNotGiveTheShadowInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCardsToHand(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"),
				scn.GetFreepsCard("troop4"), scn.GetFreepsCard("troop5"));

		var tongues = scn.GetShadowCard("tongues");
		var tongues2 = scn.GetShadowCard("tongues2");
		scn.MoveCardsToSupportArea(tongues, tongues2);

		scn.StartGame();
		scn.AddTokensToCard(tongues, 2);
		scn.AddTokensToCard(tongues2, 2);
		scn.FreepsRefreshDecision();

		// Four [raider] tokens in all, but three on neither card.
		assertEquals(2, scn.GetCultureTokensOn(tongues));
		assertEquals(2, scn.GetCultureTokensOn(tongues2));
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());

		scn.AddTokensToCard(tongues2, 1);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetCultureTokensOn(tongues2));
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
