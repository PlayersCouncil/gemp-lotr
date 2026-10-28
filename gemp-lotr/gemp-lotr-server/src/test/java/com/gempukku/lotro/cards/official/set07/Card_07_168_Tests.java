package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_168_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thief", "7_168");
					put("explorer", "4_250");   // Southron Explorer: a [raider] Southron Man; a regroup ability used only on purpose
					// Easterling Axeman: a [raider] Easterling Man; strength +3 and fierce only at 2 burdens
					put("axeman", "4_224");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul and no Man; his text needs 3 or 5 burdens
					put("khamul", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a Man who is not [raider]; no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf; a skirmish ability only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard; a strength modifier only
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit; his abilities are used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man; a skirmish ability only
					put("bounder", "1_286");    // Bounder: a Hobbit ally; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronThiefStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Southron Thief
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.
		 * <b>Archery:</b> Spot another [raider] Man, spot 6 companions, and remove a threat to exert a companion.
		*/

		var scn = GetScenario();

		var thief = scn.GetFreepsCard("thief");

		assertEquals("Southron Thief", thief.getBlueprint().getTitle());
		assertNull(thief.getBlueprint().getSubtitle());
		assertFalse(thief.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, thief.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, thief.getBlueprint().getCulture());
		assertEquals(CardType.MINION, thief.getBlueprint().getCardType());
		assertEquals(Race.MAN, thief.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(thief, Keyword.SOUTHRON));
		assertEquals(3, thief.getBlueprint().getTwilightCost());
		assertEquals(8, thief.getBlueprint().getStrength());
		assertEquals(2, thief.getBlueprint().getVitality());
		assertEquals(4, thief.getBlueprint().getSiteNumber());
	}

		@Test
	public void ArcheryAbilityRemovesAThreatToExertAChosenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sam = scn.GetFreepsCard("sam");
		var boromir = scn.GetFreepsCard("boromir");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli, gandalf, sam, boromir);
		scn.MoveAlliesToTable(bounder);

		var thief = scn.GetShadowCard("thief");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.MoveMinionsToTable(thief, explorer);
		scn.AddThreats(1);
		// Vitality 3 with 2 wounds: exhausted, so he cannot be exerted.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		// Exactly 6 companions, the Ring-bearer included; another [raider] Man, the Explorer.
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.ShadowActionAvailable(thief));

		scn.ShadowUseCardAction(thief);
		assertEquals(0, scn.GetThreats());

		// Companions of four cultures and races, the Ring-bearer among them; not the exhausted Dwarf, not an
		// ally, not a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, gandalf, sam, boromir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli, bounder, thief, explorer));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gandalf);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(thief));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedBesideARaiderNazgulAndAManOfAnotherCultureUntilAnEasterlingRaiderManIsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sam = scn.GetFreepsCard("sam");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, gimli, gandalf, sam, boromir);

		var thief = scn.GetShadowCard("thief");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(thief, khamul, brigand);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		// The Thief himself is a [raider] Man but not "another"; Khamul is no Man, the Brigand not [raider].
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(thief));

		scn.MoveMinionsToTable(axeman);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.ShadowActionAvailable(thief));
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWithFiveCompanionsAndAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli, gandalf, sam);
		scn.MoveAlliesToTable(bounder);

		var thief = scn.GetShadowCard("thief");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.MoveMinionsToTable(thief, explorer);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		// Five companions with the Ring-bearer, and an ally who is no companion: one short.
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(thief));
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWithNoThreatToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sam = scn.GetFreepsCard("sam");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, gimli, gandalf, sam, boromir);

		var thief = scn.GetShadowCard("thief");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.MoveMinionsToTable(thief, explorer);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(thief));
	}

	@Test
	public void WithEveryCompanionExhaustedItIsOfferedAndTheThreatIsRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sam = scn.GetFreepsCard("sam");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, gimli, gandalf, sam, boromir);

		var thief = scn.GetShadowCard("thief");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.MoveMinionsToTable(thief, explorer);
		scn.AddThreats(1);
		// Each companion left with 1 vitality: none can be exerted.
		scn.AddWoundsToChar(frodo, scn.GetVitality(frodo) - 1);
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(gandalf, 3);
		scn.AddWoundsToChar(sam, 3);
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetVitality(frodo));
		assertEquals(1, scn.GetVitality(aragorn));
		assertEquals(1, scn.GetVitality(gimli));
		assertEquals(1, scn.GetVitality(gandalf));
		assertEquals(1, scn.GetVitality(sam));
		assertEquals(1, scn.GetVitality(boromir));
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(thief));

		scn.ShadowUseCardAction(thief);
		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetVitality(frodo));
		assertEquals(1, scn.GetVitality(aragorn));
		assertEquals(1, scn.GetVitality(gimli));
		assertEquals(1, scn.GetVitality(gandalf));
		assertEquals(1, scn.GetVitality(sam));
		assertEquals(1, scn.GetVitality(boromir));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
