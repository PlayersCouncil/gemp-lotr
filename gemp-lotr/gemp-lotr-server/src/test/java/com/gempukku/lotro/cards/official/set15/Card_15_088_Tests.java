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

public class Card_15_088_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horde1", "15_88");
					put("horde2", "15_88");
					put("horde3", "15_88");
					put("horde4", "15_88");
					put("corps", "11_73");      // Corps of Harad: a [men] Man minion, fierce only at a battleground (none on this path)
					put("hordeofharad", "11_85"); // Horde of Harad: a second [men] Man title, the same text
					put("ted", "18_75");        // Ted Sandyman: a [men] minion who is a Hobbit, no Man
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man minion with no text
					// Grieving the Fallen: another [men] condition on a companion; its archery bar and its discard at
					// the start of the regroup phase act on nothing here
					put("grieving", "15_81");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion
					put("gimli", "15_5");       // Gimli, Eager Hunter: a [dwarven] Dwarf companion
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion with no text
					put("barliman", "1_70");    // Barliman Butterbur: an ally, who is no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PursuingHordeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Pursuing Horde
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype:
		 * Resistance: -1
		 * Game Text: To play, spot a [men] Man. Bearer must be a companion (except the Ring-bearer). Limit 1 per companion.<br>Each time the fellowship moves, add (1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horde1");

		assertEquals("Pursuing Horde", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(-1, card.getBlueprint().getResistance());
	}

	@Test
	public void ToPlaySpotAMenManAndItGoesOnACompanionWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetShadowCard("horde1");
		var corps = scn.GetShadowCard("corps");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(horde, corps);

		scn.StartGame();
		assertInHand(horde, corps);
		scn.MoveMinionsToTable(ted, brigand);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [men] minion who is no Man and a Man of another culture: not playable.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInPlay(ted, brigand);
		assertInHand(horde, corps);
		assertFalse(scn.ShadowPlayAvailable(horde));

		scn.ShadowPlayCard(corps);
		assertInPlay(corps);
		assertTrue(scn.ShadowPlayAvailable(horde));
		scn.ShadowPlayCard(horde);

		// Companions of two cultures and races are offered; the Ring-bearer, the ally and the minions are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, barliman, corps, ted, brigand));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		assertAttachedTo(horde, gimli);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsPlayableSpottingAMenManOfAnotherTitle() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetShadowCard("horde1");
		var hordeofharad = scn.GetShadowCard("hordeofharad");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(horde);

		scn.StartGame();
		assertInHand(horde);
		scn.MoveMinionsToTable(hordeofharad);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInPlay(hordeofharad);
		assertInHand(horde);
		assertTrue(scn.ShadowPlayAvailable(horde));
		scn.ShadowPlayCard(horde);

		// Aragorn, the one companion who is not the Ring-bearer, takes it unasked.
		assertAttachedTo(horde, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACompanionWhoBearsOneIsNotOfferedAnotherAndWithEveryCompanionBearingOneItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde1 = scn.GetShadowCard("horde1");
		var horde2 = scn.GetShadowCard("horde2");
		var horde3 = scn.GetShadowCard("horde3");
		var horde4 = scn.GetShadowCard("horde4");
		var grieving = scn.GetShadowCard("grieving");
		var corps = scn.GetShadowCard("corps");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, gimli, knight);
		scn.AttachCardsTo(aragorn, horde1);
		scn.AttachCardsTo(gimli, grieving);
		scn.MoveCardsToHand(horde2, horde3, horde4);

		scn.StartGame();
		assertAttachedTo(horde1, aragorn);
		assertAttachedTo(grieving, gimli);
		assertInHand(horde2, horde3, horde4);
		scn.MoveMinionsToTable(corps);
		scn.FreepsPass();

		// Aragorn bears a Pursuing Horde already; Gimli bears another [men] condition, which does not count against
		// the limit; the Knight bears nothing.
		assertTrue(scn.ShadowPlayAvailable(horde2));
		scn.ShadowPlayCard(horde2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, knight));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		scn.ShadowChooseCard(knight);
		assertAttachedTo(horde2, knight);

		// Gimli is the one companion left without one, and takes the third copy unasked.
		scn.ShadowPlayCard(horde3);
		assertAttachedTo(horde3, gimli);
		assertAttachedTo(grieving, gimli);

		// Every companion but the Ring-bearer bears one: the fourth cannot be played, the [men] Man still spotted.
		assertInPlay(corps);
		assertInHand(horde4);
		assertFalse(scn.ShadowPlayAvailable(horde4));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMoveAddsOneAndARegroupMoveAddsItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetShadowCard("horde1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, horde);

		scn.StartGame();
		assertAttachedTo(horde, aragorn);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		// The move to Ettenmoors: 2 for its Shadow number + 2 for the companions + 1 from the Horde.
		// Without the Horde it would be 4.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsChooseToMove();
		// The regroup move to Rivendell Valley: 0 for its Shadow number + 2 for the companions + 1 again.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetTwilight());
		assertAttachedTo(horde, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheRegroupDecisionAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetShadowCard("horde1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, horde);

		scn.StartGame();
		assertAttachedTo(horde, aragorn);
		scn.FreepsPass();
		scn.SkipToMovementDecision();

		// A card in hand, so that the stay is followed by the reconcile and the turn does not end at once.
		scn.SetFreepsHand(scn.GetFreepsCard("knight"));
		scn.SetTwilight(2);
		assertEquals(2, scn.GetTwilight());
		scn.FreepsChooseToStay();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
	}
}
