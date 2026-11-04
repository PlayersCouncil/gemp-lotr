package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_159_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("theoden", "11_159");
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion; a skirmish ability that needs a mount
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion; stronger for the races he spots
					put("avenger", "6_27");     // Ent Avenger: [gandalf] Ent companion who is no Gandalf; stronger beside 3 Ents
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path: site 1, Eastemnet Gullies 4_325, has no terrain; site 2, Eastfold 4_331, is a plains and no
	// battleground; site 3, Barrows of Edoras 4_337, is a sanctuary only; site 4, White Rocks 4_346, is a battleground
	// and no plains.  None of their texts is used.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("theoden", "11_159");
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally, strength 4; a fellowship ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit, strength 3; a Man only beside 3 [rohan] companions or Eowyn
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man, strength 7; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheodenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Théoden, King of the Eorlingas
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: While you can spot a [gandalf] companion, Théoden gains <b>muster</b>. <helper>(At the start of the regroup phase, you may discard a card from hand to draw a card.)</helper><br>While Théoden is at a battleground or plains site each [rohan] Man is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("theoden");

		assertEquals("Théoden", card.getBlueprint().getTitle());
		assertEquals("King of the Eorlingas", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void TheodenGainsMusterOnceAGandalfCompanionIsPlayedAndNotBesideAGandalfAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var rider = scn.GetFreepsCard("rider");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(theoden, rider, aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(gandalf);

		scn.StartGame();

		// A [gandalf] ally and a companion of another culture: no muster.
		assertFalse(scn.HasKeyword(theoden, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(rider, Keyword.MUSTER));

		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		// Théoden gains it, and the other [rohan] Man does not.
		assertTrue(scn.HasKeyword(theoden, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(rider, Keyword.MUSTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGandalfCompanionWhoIsNoGandalfGivesTheodenMusterToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(theoden);
		scn.MoveCardsToHand(avenger);

		scn.StartGame();

		assertFalse(scn.HasKeyword(theoden, Keyword.MUSTER));
		scn.FreepsPlayCard(avenger);
		assertInZone(Zone.FREE_CHARACTERS, avenger);
		assertTrue(scn.HasKeyword(theoden, Keyword.MUSTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtAPlainsEachRohanManIsStrengthPlus1AndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var frodo = scn.GetRingBearer();
		var theoden = scn.GetFreepsCard("theoden");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(theoden, merry, boromir);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();

		// Eastemnet Gullies, site 1: neither a battleground nor a plains.
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(7, scn.GetStrength(theoden));
		assertEquals(4, scn.GetStrength(guma));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(4, scn.GetStrength(frodo));    // 3 + 1 for The Ruling Ring

		// The move to Eastfold, site 2, a plains: Théoden himself and the [rohan] Man ally are +1; the [rohan] Hobbit
		// (no Man beside two [rohan] companions), the [gondor] Man and Frodo are not.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(8, scn.GetStrength(theoden));
		assertEquals(5, scn.GetStrength(guma));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtABattlegroundEachRohanManIsStrengthPlus1AndAtASanctuaryNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guma = scn.GetFreepsCard("guma");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(theoden, boromir);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();
		scn.SkipToSite(3);

		// Barrows of Edoras, site 3: a sanctuary, neither a battleground nor a plains.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(7, scn.GetStrength(theoden));
		assertEquals(4, scn.GetStrength(guma));
		assertEquals(7, scn.GetStrength(boromir));

		// The move to White Rocks, site 4, a battleground and no plains.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(8, scn.GetStrength(theoden));
		assertEquals(5, scn.GetStrength(guma));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
