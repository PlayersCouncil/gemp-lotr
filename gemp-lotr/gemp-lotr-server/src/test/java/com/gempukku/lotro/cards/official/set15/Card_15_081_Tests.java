package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_081_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grieving", "15_81");
					put("corps", "11_73");      // Corps of Harad: a [men] Man minion, fierce only at a battleground (none on this path)
					put("horde", "11_85");      // Horde of Harad: a second [men] Man title, the same text, strength 13
					put("ted", "18_75");        // Ted Sandyman: a [men] minion who is a Hobbit, no Man
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man minion with no text
					put("marksman", "1_176");   // Goblin Marksman: a minion archer and nothing else
					put("weary", "4_212");      // Weary: another Shadow condition on a companion, its trigger out of the way
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion
					put("gimli", "15_5");       // Gimli, Eager Hunter: a [dwarven] Dwarf companion
					put("barliman", "1_70");    // Barliman Butterbur: an ally, who is no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrievingtheFallenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Grieving the Fallen
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Resistance: -1
		 * Game Text: To play, spot a [men] Man. Bearer must be a companion (except the Ring-bearer).<br>Bearer cannot take wounds during the archery phase. At the start of the regroup phase, discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grieving");

		assertEquals("Grieving the Fallen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(-1, card.getBlueprint().getResistance());
	}

	@Test
	public void ToPlaySpotAMenManAndItGoesOnACompanionWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grieving = scn.GetShadowCard("grieving");
		var corps = scn.GetShadowCard("corps");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(grieving, corps);

		scn.StartGame();
		assertInHand(grieving, corps);
		scn.MoveMinionsToTable(ted, brigand);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [men] minion who is no Man and a Man of another culture: not playable.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInPlay(ted, brigand);
		assertInHand(grieving, corps);
		assertFalse(scn.ShadowPlayAvailable(grieving));

		scn.ShadowPlayCard(corps);
		assertInPlay(corps);
		assertTrue(scn.ShadowPlayAvailable(grieving));
		scn.ShadowPlayCard(grieving);

		// Companions of two cultures and races are offered; the Ring-bearer, the ally and the minions are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, barliman, corps, ted, brigand));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		assertAttachedTo(grieving, gimli);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsPlayableSpottingAMenManOfAnotherTitle() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grieving = scn.GetShadowCard("grieving");
		var horde = scn.GetShadowCard("horde");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(grieving);

		scn.StartGame();
		assertInHand(grieving);
		scn.MoveMinionsToTable(horde);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInPlay(horde);
		assertInHand(grieving);
		assertTrue(scn.ShadowPlayAvailable(grieving));
		scn.ShadowPlayCard(grieving);

		// Aragorn, the one companion who is not the Ring-bearer, takes it unasked.
		assertAttachedTo(grieving, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerCannotTakeArcheryWoundsButTakesAWoundInTheSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grieving = scn.GetShadowCard("grieving");
		var horde = scn.GetShadowCard("horde");
		var marksman = scn.GetShadowCard("marksman");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, grieving);

		scn.StartGame();
		assertAttachedTo(grieving, aragorn);
		scn.MoveMinionsToTable(horde, marksman);
		scn.SkipToArcheryWounds();

		// The Marksman's archery wound: the bearer cannot take it, the other companions can.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, horde);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertAttachedTo(grieving, aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();

		// In the skirmish phase the bar is off: 8 against 13, Aragorn loses and takes the wound.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsDiscardedAtTheStartOfTheRegroupPhaseAndNotBefore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grieving = scn.GetShadowCard("grieving");
		var weary = scn.GetShadowCard("weary");
		var horde = scn.GetShadowCard("horde");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, grieving);
		scn.AttachCardsTo(gimli, weary);

		scn.StartGame();
		assertAttachedTo(grieving, aragorn);
		assertAttachedTo(weary, gimli);
		scn.MoveMinionsToTable(horde);
		scn.SkipToAssignments();

		// Still on its bearer at the assignment phase and through the skirmish.
		assertAttachedTo(grieving, aragorn);
		scn.FreepsAssignToMinions(aragorn, horde);
		scn.FreepsResolveSkirmish(aragorn);
		assertAttachedTo(grieving, aragorn);
		scn.PassSkirmishActions();

		// At the start of the regroup phase it goes to its owner's discard pile; the other Shadow condition stays.
		assertInZone(Zone.DISCARD, grieving);
		assertNotAttachedTo(grieving, aragorn);
		assertAttachedTo(weary, gimli);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
