package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_223_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nertea", "11_223");
					// Úlairë Nelya, Ringwraith in Twilight: a [wraith] Nazgûl of strength 10; her text needs 2 burdens
					// or 2 wounds on the Ring-bearer, and there are none
					put("nelya", "2_84");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl of strength 12; his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Creeper: a [wraith] Orc, no Nazgûl, of strength 8; its text needs 3 burdens
					put("creeper", "8_75");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Glóin: a skirmish ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: abilities not used
					put("merry", "1_303");      // Merry, From O'er the Brandywine: cost 1; strength +2 while bearing a weapon
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally, no companion
				}},
				new HashMap<>()
				{{
					// Site 1, Fangorn Glade 11_239, is a forest; site 2, Abandoned Mine Shaft 13_185, is underground
					// and no forest.
					putAll(VirtualTableScenario.ShadowsSites);
					// Hill of Sight: a forest that is a battleground too.  Its text changes only resistance.
					put("site8", "12_188");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void UlaireNerteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Úlairë Nertëa, Ninth of the Nine Riders
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br>While Úlairë Nertëa is at a forest site, he is strength +2.<br>While you can spot 6 companions, each Nazgûl is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nertea");

		assertEquals("Úlairë Nertëa", card.getBlueprint().getTitle());
		assertEquals("Ninth of the Nine Riders", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtAForestSiteNerteaIsStrengthPlus2AndAwayFromOneHeIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");
		var glade = scn.GetFreepsSite("site1");

		scn.StartGame(glade);
		scn.MoveMinionsToTable(nertea, nelya);
		assertSame(glade, scn.GetCurrentSite());
		assertTrue(scn.HasKeyword(glade, Keyword.FOREST));

		// At Fangorn Glade, a forest: 9 + 2.  Nelya, another minion at the same site, is untouched.
		assertEquals(11, scn.GetStrength(nertea));
		assertEquals(10, scn.GetStrength(nelya));

		scn.FreepsPass();
		var shaft = scn.GetShadowSite("site2");
		scn.ShadowChooseCardBPFromSelection(shaft);

		// At Abandoned Mine Shaft, underground and no forest.
		assertSame(shaft, scn.GetCurrentSite());
		assertFalse(scn.HasKeyword(shaft, Keyword.FOREST));
		assertEquals(9, scn.GetStrength(nertea));
		assertEquals(10, scn.GetStrength(nelya));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtAForestThatIsABattlegroundNerteaIsStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var shaft = scn.GetFreepsSite("site2");

		scn.StartGame(shaft);
		scn.MoveMinionsToTable(nertea);
		assertEquals(9, scn.GetStrength(nertea));

		scn.FreepsPass();
		var hill = scn.GetShadowSite("site8");
		scn.ShadowChooseCardBPFromSelection(hill);

		assertSame(hill, scn.GetCurrentSite());
		assertTrue(scn.HasKeyword(hill, Keyword.FOREST));
		assertTrue(scn.HasKeyword(hill, Keyword.BATTLEGROUND));
		assertEquals(11, scn.GetStrength(nertea));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSixCompanionsEachNazgulIsStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");
		var merry = scn.GetFreepsCard("merry");
		// Frodo and four more: five companions, and an ally beside them.
		scn.MoveCompanionsToTable("aragorn", "gimli", "boromir", "sam");
		scn.MoveAlliesToTable("thrarin");
		scn.MoveCardsToHand(merry);

		// Abandoned Mine Shaft is no forest, so Nertëa's first sentence does nothing here.
		scn.StartGame(scn.GetFreepsSite("site2"));
		scn.MoveMinionsToTable(nertea, nelya, khamul, creeper);
		scn.FreepsRefreshDecision();

		// Five companions: nobody changes.
		assertEquals(9, scn.GetStrength(nertea));
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(8, scn.GetStrength(creeper));

		// The sixth companion played.
		scn.FreepsPlayCard(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);

		assertEquals(11, scn.GetStrength(nertea));
		assertEquals(12, scn.GetStrength(nelya));
		assertEquals(14, scn.GetStrength(khamul));
		assertEquals(8, scn.GetStrength(creeper));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
