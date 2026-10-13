package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_036_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scroll", "9_36");

					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, cost 3; a skirmish ability only
					// Gondorian Merchant: [gondor] Man ally; a maneuver ability used only on purpose
					put("merchant", "7_97");
					// Pippin, Trollslayer: a [gondor] Hobbit companion (to play, spot a [gondor] companion: cheated in)
					put("pippin", "103_31");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; Valiant only
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; a skirmish ability only

					// [gondor] artifacts
					put("shards", "3_44");      // The Shards of Narsil: support area, cost 1, no requirement; fellowship abilities used only on purpose
					put("stone", "9_37");       // Seeing Stone of Minas Anor: support area (to play, spot a [gondor] Man: cheated in)
					put("sapling", "9_35");     // Sapling of the White Tree: support area, cost 1; to play, spot an unbound [gondor] Man
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: a hand weapon, no ring; bearer must be a [gondor] Man, cost 2
					put("barahir", "6_55");     // Ring of Barahir: a [gondor] ring, cost 0; bearer must be Aragorn

					// Items that are not [gondor] artifacts, and rings of another culture
					put("cloak", "15_68");      // Ranger's Cloak: a [gondor] POSSESSION on a [gondor] Man
					put("guile", "9_8");        // Ring of Guile: a [dwarven] ring, cost 0; bearer must be a Dwarf
					put("fury", "9_7");         // Ring of Fury: a [dwarven] ring, cost 0; bearer must be a Dwarf

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ScrollofIsildurStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Scroll of Isildur
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Artifact
		 * Subtype: Support Area
		 * Game Text: <b>Tale</b>. To play, spot a [gondor] Man.<br>The Ring-bearer is resistance +1 for each [gondor] artifact you can spot (limit +3).<br><b>Fellowship:</b> Discard this artifact to play a ring from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scroll");

		assertEquals("Scroll of Isildur", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAGondorManCompanionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scroll = scn.GetFreepsCard("scroll");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(pippin, guard);
		scn.MoveCardsToHand(scroll, boromir);

		scn.StartGame();

		// A [gondor] Hobbit and a [rohan] Man are no [gondor] Man.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(scroll));

		scn.FreepsPlayCard(boromir);
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertTrue(scn.FreepsPlayAvailable(scroll));

		scn.FreepsPlayCard(scroll);
		assertInZone(Zone.SUPPORT, scroll);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGondorManWhoIsAnAllyIsSpottedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scroll = scn.GetFreepsCard("scroll");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable("pippin", "guard");
		scn.MoveCardsToSupportArea(merchant);
		scn.MoveCardsToHand(scroll);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(scroll));
		scn.FreepsPlayCard(scroll);
		assertInZone(Zone.SUPPORT, scroll);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheRingBearerIsResistancePlusOneForEachGondorArtifactAndNoOtherItem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var scroll = scn.GetFreepsCard("scroll");
		var shards = scn.GetFreepsCard("shards");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var narsil = scn.GetFreepsCard("narsil");
		scn.MoveCompanionsToTable(boromir, sam, gimli);
		// A [gondor] possession and a [dwarven] artifact in play are not counted.
		scn.AttachCardsTo(boromir, scn.GetFreepsCard("cloak"));
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("guile"));
		scn.MoveCardsToHand(scroll, shards, narsil);

		scn.StartGame();

		int frodoBefore = scn.GetResistance(frodo);
		int samBefore = scn.GetResistance(sam);

		// The Scroll is itself a [gondor] artifact.
		scn.FreepsPlayCard(scroll);
		assertEquals(frodoBefore + 1, scn.GetResistance(frodo));
		assertEquals(samBefore, scn.GetResistance(sam));

		scn.FreepsPlayCard(shards);
		assertInZone(Zone.SUPPORT, shards);
		assertEquals(frodoBefore + 2, scn.GetResistance(frodo));
		// Not a Ring-bound companion who is not the Ring-bearer.
		assertEquals(samBefore, scn.GetResistance(sam));

		// A [gondor] artifact that is borne, and not in the support area, is counted as well: Boromir is the one
		// [gondor] Man, so he takes it with nothing asked.
		scn.FreepsPlayCard(narsil);
		assertAttachedTo(narsil, boromir);
		assertEquals(frodoBefore + 3, scn.GetResistance(frodo));
		assertEquals(samBefore, scn.GetResistance(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheResistanceBonusStopsAtPlusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var scroll = scn.GetFreepsCard("scroll");
		var sapling = scn.GetFreepsCard("sapling");
		scn.MoveCompanionsToTable("boromir");
		scn.MoveCardsToSupportArea(scn.GetFreepsCard("shards"), scn.GetFreepsCard("stone"));
		scn.MoveCardsToHand(scroll, sapling);

		scn.StartGame();

		int frodoBefore = scn.GetResistance(frodo);

		// Three [gondor] artifacts with the Scroll.
		scn.FreepsPlayCard(scroll);
		assertEquals(frodoBefore + 3, scn.GetResistance(frodo));

		// A fourth adds nothing.
		scn.FreepsPlayCard(sapling);
		assertInZone(Zone.SUPPORT, sapling);
		assertEquals(frodoBefore + 3, scn.GetResistance(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionDiscardsTheScrollToPlayARingFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scroll = scn.GetFreepsCard("scroll");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var barahir = scn.GetFreepsCard("barahir");
		var guile = scn.GetFreepsCard("guile");
		var narsil = scn.GetFreepsCard("narsil");
		var shadowFury = scn.GetShadowCard("fury");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(scroll);
		// Rings of two cultures, and an artifact that is no ring, each with a bearer to take it.
		scn.MoveCardsToDiscard(barahir, guile, narsil);
		// A ring in the other player's discard pile, of a title that lies in none of this player's piles (a
		// pile's choice is read by blueprint).
		scn.MoveCardsToDiscard(shadowFury);
		scn.MoveCardsToHand(scn.GetFreepsCard("fury"));

		scn.StartGame();

		assertInZone(Zone.SUPPORT, scroll);
		assertTrue(scn.FreepsActionAvailable(scroll));
		scn.FreepsUseCardAction(scroll);

		assertInZone(Zone.DISCARD, scroll);
		assertTrue(scn.FreepsHasCardChoiceAvailable(barahir, guile));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(narsil, shadowFury));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guile);

		// Gimli is the one Dwarf, and takes it with nothing asked.
		assertAttachedTo(guile, gimli);
		assertInZone(Zone.DISCARD, barahir, narsil);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoRingThatCanBePlayedInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scroll = scn.GetFreepsCard("scroll");
		var gimli = scn.GetFreepsCard("gimli");
		var guile = scn.GetFreepsCard("guile");
		var narsil = scn.GetFreepsCard("narsil");
		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToSupportArea(scroll);
		// Narsil could go on Aragorn but is no ring; the Ring of Guile has no Dwarf to bear it.
		scn.MoveCardsToDiscard(guile, narsil);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(scroll));

		scn.MoveCompanionsToTable(gimli);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(scroll));
	}

	@Test
	public void IsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scroll = scn.GetFreepsCard("scroll");
		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToSupportArea(scroll);
		scn.MoveCardsToDiscard(scn.GetFreepsCard("barahir"));

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(scroll));
	}
}
