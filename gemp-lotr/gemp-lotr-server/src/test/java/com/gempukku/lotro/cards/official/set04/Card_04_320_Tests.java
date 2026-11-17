package com.gempukku.lotro.cards.official.set04;

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

public class Card_04_320_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("storeroom", "4_320");
					put("merry", "1_302");      // Merry, Friend to Sam: a [shire] Hobbit companion, unbound, vitality 4; a skirmish ability only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, unbound, vitality 4; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit companion who is Ring-bound by rule; his abilities used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound companion who is no Hobbit; a skirmish ability only
					put("bounder", "1_286");    // Bounder: a Hobbit ally, never unbound; a skirmish ability only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength bonus of his own only
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf; a strength bonus beside 3 Ents only
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, for a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StoreroomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Store-room
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Heal an unbound Hobbit once (or twice if you spot Gandalf).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("storeroom");

		assertEquals("Store-room", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithoutGandalfItHealsAnUnboundHobbitOfAnyCultureOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var storeroom = scn.GetFreepsCard("storeroom");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(merry, pippin, sam, gimli, avenger);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(storeroom);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(merry, 2);
		scn.AddWoundsToChar(pippin, 2);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(bounder, 1);
		scn.AddWoundsToChar(avenger, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(avenger));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(storeroom));
		scn.FreepsPlayCard(storeroom);

		// Ent Avenger is a [gandalf] companion, and no Gandalf: one heal.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, gimli, bounder, avenger));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertInDiscard(storeroom);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithGandalfItHealsTheOneUnboundHobbitChosenTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var storeroom = scn.GetFreepsCard("storeroom");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(merry, pippin, gandalf);
		scn.MoveCardsToHand(storeroom);

		scn.StartGame();
		scn.AddWoundsToChar(merry, 2);
		scn.AddWoundsToChar(pippin, 2);
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(pippin));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPlayCard(storeroom);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		scn.FreepsChooseCard(pippin);

		// Both heals go to Pippin; Merry, wounded as well, is not healed.
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(2, scn.GetWoundsOn(merry));
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertInDiscard(storeroom);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoWoundedUnboundHobbitItIsPlayedAndHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var storeroom = scn.GetFreepsCard("storeroom");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(merry, sam, gandalf);
		scn.MoveCardsToHand(storeroom);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(sam, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(merry));

		// Merry, the only unbound Hobbit, has no wound to heal; an event's target is no requirement.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsPlayAvailable(storeroom));
		scn.FreepsPlayCard(storeroom);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertInDiscard(storeroom);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void InTheManeuverPhaseItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var storeroom = scn.GetFreepsCard("storeroom");
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(storeroom);

		scn.StartGame();
		scn.AddWoundsToChar(merry, 1);
		assertEquals(1, scn.GetWoundsOn(merry));
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(storeroom));
		assertEquals(1, scn.GetWoundsOn(merry));
	}
}
