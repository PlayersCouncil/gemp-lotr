package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_057_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trap", "13_57");

					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: [gollum] minion; "Regroup: Discard Gollum to exert a companion or ally", never used
					put("wraithgollum", "103_66");  // Gollum, Half a Wraith Himself: the [wraith] Gollum; his texts answer a Nazgûl played or are assignment actions
					put("shelob", "8_26");          // Shelob, Last Child of Ungoliant: the [gollum] minion who is no Gollum

					put("sam", "1_311");            // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer; a fellowship ability, never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TrapIsSprungStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Trap Is Sprung
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: To play, discard Gollum from play.<br>Heal the Ring-bearer to add a burden. If the fellowship is not in region 1, you may repeat this for each other wound on the Ring-bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trap");

		assertEquals("Trap Is Sprung", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsGollumFromPlayToHealTheRingBearerAndAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trap = scn.GetShadowCard("trap");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(2);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());

		// The regroup phase at site 3, in region 1; minions that lived through the skirmishes stand there.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(gollum, shelob);
		scn.MoveCardsToHand(trap);
		scn.SetTwilight(3);
		assertInHand(trap);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(trap));
		scn.ShadowPlayCard(trap);

		// Gollum is the one card that can pay, so nothing is asked; Shelob stays.
		assertInDiscard(gollum, trap);
		assertInZone(Zone.SHADOW_CHARACTERS, shelob);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InRegion1TheHealIsNotRepeatedAndTheWraithGollumPays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trap = scn.GetShadowCard("trap");
		var gollum = scn.GetShadowCard("wraithgollum");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(2);
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(gollum);
		scn.MoveCardsToHand(trap);
		scn.SetTwilight(3);
		assertInHand(trap);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(trap));
		scn.ShadowPlayCard(trap);

		// At site 3 the fellowship is in region 1: one heal, one burden, and no question of more.
		assertInDiscard(gollum, trap);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.ShadowDecisionAvailable("heal"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotPlayableWithoutGollum() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trap = scn.GetShadowCard("trap");
		var shelob = scn.GetShadowCard("shelob");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(2);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(shelob);
		scn.MoveCardsToHand(trap);
		scn.SetTwilight(3);
		assertInHand(trap);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(trap));
	}

	@Test
	public void NotPlayableWhileTheRingBearerIsUnwounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trap = scn.GetShadowCard("trap");
		var gollum = scn.GetShadowCard("gollum");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(2);
		// Sam, Ring-bound and wounded, is not the Ring-bearer.
		scn.AddWoundsToChar(sam, 1);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(gollum);
		scn.MoveCardsToHand(trap);
		scn.SetTwilight(3);
		assertInHand(trap);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(trap));
	}

	@Test
	public void OutsideRegion1TheFirstHealIsNotAskedAndIsRepeatedForEachOtherWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trap = scn.GetShadowCard("trap");
		var gollum = scn.GetShadowCard("gollum");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(sam, 2);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());

		// The regroup phase at site 4, in region 2.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(gollum);
		scn.MoveCardsToHand(trap);
		scn.SetTwilight(3);
		assertInHand(trap);
		scn.FreepsPass();

		scn.ShadowPlayCard(trap);
		// The first heal and burden are no "may".
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());

		// Two other wounds on the Ring-bearer: it may be done twice more; Sam's wounds are not counted.
		assertTrue(scn.ShadowDecisionAvailable("heal"));
		scn.ShadowChooseYes();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.ShadowDecisionAvailable("heal"));
		scn.ShadowChooseYes();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(sam));
		assertFalse(scn.ShadowDecisionAvailable("heal"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OutsideRegion1TheRepeatMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trap = scn.GetShadowCard("trap");
		var gollum = scn.GetShadowCard("gollum");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddWoundsToChar(frodo, 2);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(gollum);
		scn.MoveCardsToHand(trap);
		scn.SetTwilight(3);
		assertInHand(trap);
		scn.FreepsPass();

		scn.ShadowPlayCard(trap);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());

		assertTrue(scn.ShadowDecisionAvailable("heal"));
		scn.ShadowChooseNo();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.ShadowDecisionAvailable("heal"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OutsideRegion1WithOneWoundNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trap = scn.GetShadowCard("trap");
		var gollum = scn.GetShadowCard("gollum");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(gollum);
		scn.MoveCardsToHand(trap);
		scn.SetTwilight(3);
		assertInHand(trap);
		scn.FreepsPass();

		scn.ShadowPlayCard(trap);
		// No other wound: the heal and burden are done, and there is nothing to repeat.
		assertFalse(scn.ShadowDecisionAvailable("heal"));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
