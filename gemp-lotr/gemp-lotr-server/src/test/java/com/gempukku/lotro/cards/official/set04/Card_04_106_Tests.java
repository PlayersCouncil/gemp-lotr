package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_106_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wellmet", "4_106");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDeadPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wellmet", "4_106");
					// Elrond, Lord of Rivendell: the Elf, an ally; his start-of-turn heal reaches allies of
					// site 3 only, and his fellowship ability is not used
					put("elrond", "1_40");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the [gondor] Man
					put("gimli", "5_7");        // Gimli, Skilled Defender: the Dwarf; a trigger on winning a skirmish only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: cost 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WellMetIndeedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Well Met Indeed
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 5
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Spot an Elf, a [gondor] Man, and a Dwarf to play Gandalf from your dead pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wellmet");

		assertEquals("Well Met Indeed", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(5, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpotsAnElfAGondorManAndADwarfToPlayGandalfFromTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDeadPileScenario();

		var wellmet = scn.GetFreepsCard("wellmet");
		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveAlliesToTable(elrond);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToDeadPile(gandalf);
		scn.MoveCardsToHand(wellmet);

		scn.StartGame();

		assertInZone(Zone.DEAD, gandalf);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertTrue(scn.FreepsPlayAvailable(wellmet));

		scn.FreepsPlayCard(wellmet);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertInZone(Zone.DISCARD, wellmet);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	protected VirtualTableScenario GetSpotScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wellmet", "4_106");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: cost 4
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf companion; her text reads Nazgûl only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; a skirmish ability only
					// Gondorian Merchant: the one [gondor] ally, a Man; his ability needs initiative and is not used.
					put("merchant", "7_97");
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally; his ability is not used
					// Pippin, Trollslayer: a [gondor] companion who is a Hobbit, no Man.  Cheated in, so his "To
					// play" does not come up; his skirmish ability is not used.
					put("pippin", "103_31");
					// Éomer, Third Marshal of Riddermark: a Man of [rohan], not [gondor]; his texts touch his cost
					// and his skirmishes only.
					put("eomer", "4_267");
					// Ent Avenger: a [gandalf] companion who is not Gandalf.
					put("avenger", "6_27");
					// Gandalf, Powerful Guide: "To play, spot 2 Hobbits."  Beside the Ring-bearer alone he cannot be
					// played; his triggers come up only once he is in play.
					put("guide", "15_29");
					put("merry", "1_302");      // Merry, Friend to Sam: the second Hobbit for him; his ability is not used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheGondorManAndTheDwarfMayBeAllies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSpotScenario();

		var wellmet = scn.GetFreepsCard("wellmet");
		var gandalf = scn.GetFreepsCard("gandalf");
		var arwen = scn.GetFreepsCard("arwen");
		var merchant = scn.GetFreepsCard("merchant");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(merchant, grimir);
		scn.MoveCardsToDeadPile(gandalf);
		scn.MoveCardsToHand(wellmet);

		scn.StartGame();

		assertInZone(Zone.DEAD, gandalf);
		assertTrue(scn.FreepsPlayAvailable(wellmet));
		scn.FreepsPlayCard(wellmet);

		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertInZone(Zone.DISCARD, wellmet);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotPlayableWithoutAGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSpotScenario();

		var wellmet = scn.GetFreepsCard("wellmet");
		var gandalf = scn.GetFreepsCard("gandalf");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		var pippin = scn.GetFreepsCard("pippin");
		var eomer = scn.GetFreepsCard("eomer");
		var aragorn = scn.GetFreepsCard("aragorn");
		// A [gondor] companion who is no Man, and a Man who is not [gondor].
		scn.MoveCompanionsToTable(arwen, gimli, pippin, eomer);
		scn.MoveCardsToDeadPile(gandalf);
		scn.MoveCardsToHand(wellmet);

		scn.StartGame();

		assertInZone(Zone.DEAD, gandalf);
		assertInHand(wellmet);
		assertFalse(scn.FreepsPlayAvailable(wellmet));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Aragorn makes it playable.
		scn.MoveCompanionsToTable(aragorn);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(wellmet));
	}

	@Test
	public void IsNotPlayableWithoutAnElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSpotScenario();

		var wellmet = scn.GetFreepsCard("wellmet");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToDeadPile(gandalf);
		scn.MoveCardsToHand(wellmet);

		scn.StartGame();

		assertInZone(Zone.DEAD, gandalf);
		assertInHand(wellmet);
		assertFalse(scn.FreepsPlayAvailable(wellmet));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotPlayableWithoutADwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSpotScenario();

		var wellmet = scn.GetFreepsCard("wellmet");
		var gandalf = scn.GetFreepsCard("gandalf");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveCardsToDeadPile(gandalf);
		scn.MoveCardsToHand(wellmet);

		scn.StartGame();

		assertInZone(Zone.DEAD, gandalf);
		assertInHand(wellmet);
		assertFalse(scn.FreepsPlayAvailable(wellmet));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotPlayableUntilAGandalfInTheDeadPileCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSpotScenario();

		var wellmet = scn.GetFreepsCard("wellmet");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var guide = scn.GetFreepsCard("guide");
		var merry = scn.GetFreepsCard("merry");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(arwen, aragorn, gimli);
		// A Gandalf in the discard pile, and a [gandalf] companion who is no Gandalf in the dead pile.
		scn.MoveCardsToDiscard(gandalf);
		scn.MoveCardsToDeadPile(avenger);
		scn.MoveCardsToHand(wellmet);

		scn.StartGame();

		assertInZone(Zone.DISCARD, gandalf);
		assertInZone(Zone.DEAD, avenger);
		assertFalse(scn.FreepsPlayAvailable(wellmet));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Gandalf, Powerful Guide in the dead pile, with the Ring-bearer the one Hobbit: he cannot be played.
		scn.MoveCardsToDeadPile(guide);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.DEAD, guide);
		assertFalse(scn.FreepsPlayAvailable(wellmet));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// With Merry, a second Hobbit, he can, and it is he who is played.
		scn.MoveCompanionsToTable(merry);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(wellmet));
		scn.FreepsPlayCard(wellmet);
		assertInZone(Zone.FREE_CHARACTERS, guide);
		assertInZone(Zone.DEAD, avenger);
		assertInZone(Zone.DISCARD, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
