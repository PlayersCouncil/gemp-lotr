package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_035_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "18_35");
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: enduring, fierce; damage +1 in a fierce skirmish only
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: a [gollum] minion who is no Shelob; a regroup ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit, Ring-bound, not the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf; a skirmish ability used only on purpose
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] ally, no companion; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Great Ring: no strength, and it answers no wound while it is not worn, so the threat wound
				// on Frodo is not asked about.
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void StingofShelobStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Sting of Shelob
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 10
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: To play, spot Shelob.<br>The Free Peoples player must choose two companions in play (except the Ring- bearer). He or she then chooses to place one of those companions in the dead pile and return the other to his or her hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sting");

		assertEquals("Sting of Shelob", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(10, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilShelobIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetShadowCard("sting");
		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(sting);
		assertInHand(sting);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// The minions stand in the regroup phase as minions that lived through the skirmishes do.
		scn.MoveMinionsToTable(gollum);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [gollum] minion who is no Shelob.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(sting));

		scn.MoveMinionsToTable(shelob);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(sting));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void OnlyCompanionsWhoAreNotTheRingBearerAreChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetShadowCard("sting");
		var shelob = scn.GetShadowCard("shelob");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(sam, aragorn, gimli);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(sting);
		assertInHand(sting);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(shelob);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(sting));
		scn.ShadowPlayCard(sting);

		// The Free Peoples player chooses: companions of three cultures and races, Sam Ring-bound among them;
		// neither the Ring-bearer, nor an ally, nor a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, barliman, shelob));
		assertEquals(Boolean.TRUE, scn.AnyDecisionsAvailable(VirtualTableScenario.P1));
	}

	@Test
	public void TheFreePeoplesPlayerChoosesTwoCompanionsThenWhichOfThemDiesAndTheOtherGoesToHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetShadowCard("sting");
		var shelob = scn.GetShadowCard("shelob");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(sam, aragorn, gimli);
		scn.MoveCardsToHand(sting);
		assertInHand(sting);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(shelob);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.ShadowPlayCard(sting);

		// "Must choose two companions": both at once.
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, gimli);

		// Then which of those two is placed in the dead pile: Sam, not chosen, is not offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, frodo));
		scn.FreepsChooseCard(aragorn);

		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.GetFreepsHand().contains(gimli));
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeadCount());
		assertInZone(Zone.FREE_CHARACTERS, sam, frodo);
		assertInDiscard(sting);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOneCompanionBesideTheRingBearerHeIsKilledAndTheThreatsBecomeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetShadowCard("sting");
		var shelob = scn.GetShadowCard("shelob");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(sting);
		assertInHand(sting);

		scn.StartGame();
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(shelob);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetFreepsDeadCount());
		assertEquals(0, scn.GetFreepsHandCount());
		scn.ShadowPlayCard(sting);

		// As much as can be done (CR 4.2 "effect"): the one companion is placed in the dead pile, and nobody
		// is left to return.  Placed in the dead pile, he is killed (CR 4.2 "dead pile"): the threat is
		// removed and becomes a wound on a companion, the Ring-bearer being the one left.
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInDiscard(sting);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOnlyTheRingBearerItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetShadowCard("sting");
		var shelob = scn.GetShadowCard("shelob");
		var frodo = scn.GetRingBearer();
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(sting);
		assertInHand(sting);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(shelob);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(sting));
		scn.ShadowPlayCard(sting);

		assertInZone(Zone.FREE_CHARACTERS, frodo);
		assertInZone(Zone.SUPPORT, barliman);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(sting);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
