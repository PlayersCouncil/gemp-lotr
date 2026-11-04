package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_029_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("moot1", "6_29");
					put("moot2", "6_29");
					put("moot3", "6_29");
					put("moot4", "6_29");

					// Ents: a companion, an ally, and a second companion
					put("avenger", "6_27");       // Ent Avenger: Ent companion; strength +2 only while 3 Ents can be spotted
					put("treebeard", "4_104");    // Treebeard, Oldest Living Thing: Ent ally; a Fellowship ability used only on purpose
					put("onod", "17_26");         // Woodland Onod: Ent companion; a Skirmish ability used only on purpose
					put("enraged", "6_26");       // Enraged: another [gandalf] condition an Ent bears, limited per bearer by its own title

					// Hobbits, and an unbound companion who is no Hobbit
					put("merry", "1_302");        // Merry, Friend to Sam: unbound [shire] Hobbit; a Skirmish ability used only on purpose
					put("pippin", "103_31");      // Pippin, Trollslayer: unbound [gondor] Hobbit; cheated in past his own "To play"
					put("pippin2", "1_306");      // Pippin, Friend to Frodo: unbound [shire] Hobbit to play from hand
					put("sam", "1_311");          // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer
					put("bounder", "1_286");      // Bounder: a Hobbit ally, never unbound
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: an unbound [gandalf] companion who is no Hobbit and no Ent
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EntMootStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Ent Moot
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Strength: +2
		 * Game Text: To play, exert an unbound Hobbit. Bearer must be an Ent. Limit 1 per bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("moot1");

		assertEquals("Ent Moot", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void ToPlayAnUnboundHobbitExertsAndOnlyAnEntMayBearIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var moot = scn.GetFreepsCard("moot1");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var treebeard = scn.GetFreepsCard("treebeard");
		scn.MoveCompanionsToTable(gandalf, merry, pippin, sam, avenger);
		scn.MoveAlliesToTable(treebeard, bounder);
		scn.MoveCardsToHand(moot);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(moot));

		scn.FreepsPlayCard(moot);

		// The bearer: an Ent companion and an Ent ally, not a [gandalf] companion who is no Ent.
		assertTrue(scn.FreepsHasCardChoiceAvailable(avenger, treebeard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, frodo, merry));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(treebeard);

		// The Hobbit who exerts: unbound Hobbits of two cultures, not the Ring-bearer, a Ring-bound Sam,
		// a Hobbit ally or an unbound companion who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, bounder, gandalf));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertAttachedTo(moot, treebeard);
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyUnboundHobbitIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var moot = scn.GetFreepsCard("moot1");
		var merry = scn.GetFreepsCard("merry");
		var pippin2 = scn.GetFreepsCard("pippin2");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, merry, sam, avenger);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(moot, pippin2);
		// Merry, vitality 4, exhausted; Sam, Bounder and Gandalf could all exert.
		scn.AddWoundsToChar(merry, 3);

		scn.StartGame();

		assertEquals(3, scn.GetWoundsOn(merry));
		assertFalse(scn.FreepsPlayAvailable(moot));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// One legal play later there is an unbound Hobbit who can exert.
		scn.FreepsPlayCard(pippin2);
		assertTrue(scn.FreepsPlayAvailable(moot));
		scn.FreepsPlayCard(moot);
		assertAttachedTo(moot, avenger);
		assertEquals(1, scn.GetWoundsOn(pippin2));
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnEntBearingEntMootMayNotBearAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var moot1 = scn.GetFreepsCard("moot1");
		var moot2 = scn.GetFreepsCard("moot2");
		var moot3 = scn.GetFreepsCard("moot3");
		var moot4 = scn.GetFreepsCard("moot4");
		var enraged = scn.GetFreepsCard("enraged");
		var merry = scn.GetFreepsCard("merry");
		var avenger = scn.GetFreepsCard("avenger");
		var onod = scn.GetFreepsCard("onod");
		var treebeard = scn.GetFreepsCard("treebeard");
		scn.MoveCompanionsToTable(merry, avenger, onod);
		scn.MoveAlliesToTable(treebeard);
		scn.AttachCardsTo(avenger, moot1);
		scn.AttachCardsTo(onod, enraged);
		scn.MoveCardsToHand(moot2, moot3, moot4);

		scn.StartGame();

		assertAttachedTo(moot1, avenger);
		assertAttachedTo(enraged, onod);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsPlayAvailable(moot2));

		scn.FreepsPlayCard(moot2);
		// The Ent bearing another condition, and the one bearing nothing, may take it; the one bearing Ent Moot may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(onod, treebeard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger));
		scn.FreepsChooseCard(treebeard);
		assertAttachedTo(moot2, treebeard);
		assertEquals(1, scn.GetWoundsOn(merry));

		// Onod is the one Ent left to bear it, and Merry the one Hobbit to exert: nothing is asked.
		assertTrue(scn.FreepsPlayAvailable(moot3));
		scn.FreepsPlayCard(moot3);
		assertAttachedTo(moot3, onod);
		assertEquals(2, scn.GetWoundsOn(merry));

		// Every Ent bears one now: not playable, though Merry can still exert.
		assertFalse(scn.FreepsPlayAvailable(moot4));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
