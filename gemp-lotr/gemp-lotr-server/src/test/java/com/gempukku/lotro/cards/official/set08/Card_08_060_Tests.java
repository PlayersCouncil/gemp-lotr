package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marksman", "8_60");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetArcheryScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marksman", "8_60");
					put("numenorean", "8_49");  // Black Númenorean: a [raider] Man; fierce, and a skirmish ability that needs initiative
					put("ships", "8_65");       // Ships of Great Draught: a [raider] possession in the support area, no Man
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man with no text
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man (Easterling, no Corsair); its text needs 2 burdens
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] minion who is no Man (a Nazgul); his text needs 3 burdens
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound
					put("gimli", "1_13");       // Gimli, Son of Glóin: unbound, so that a wound unbound companions alone may take is still a choice
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HaradrimMarksmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Haradrim Marksman
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Archer</b>.<br>While you can spot another [raider] Man, Ring-bound companions cannot take archery wounds.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("marksman");

		assertEquals("Haradrim Marksman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithAnotherRaiderManRingBoundCompanionsAreNotOfferedArcheryWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetArcheryScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(sam, aragorn, gimli);

		var marksman = scn.GetShadowCard("marksman");
		var numenorean = scn.GetShadowCard("numenorean");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman, numenorean);
		scn.SkipToArcheryWounds();

		// The Marksman is the one archer: one wound, which only the unbound companions may take.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoOtherRaiderManRingBoundCompanionsAreOfferedArcheryWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetArcheryScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(sam, aragorn, gimli);

		var marksman = scn.GetShadowCard("marksman");
		var ships = scn.GetShadowCard("ships");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		// Beside the Marksman: a [raider] card that is no Man, and a Man who is not [raider].
		scn.MoveCardsToSupportArea(ships);
		scn.MoveMinionsToTable(marksman, brigand);
		scn.SkipToArcheryWounds();

		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, aragorn, gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ARaiderNazgulAndADunlandManBesideHimLeaveRingBoundCompanionsOpenToArcheryWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetArcheryScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(sam, aragorn, gimli);

		var marksman = scn.GetShadowCard("marksman");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		// Beside the Marksman: a [raider] minion who is no Man, and a Man who is not [raider].
		scn.MoveMinionsToTable(marksman, khamul, brigand);
		assertInZone(Zone.SHADOW_CHARACTERS, marksman, khamul, brigand);
		scn.SkipToArcheryWounds();

		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, aragorn, gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void BesideAnEasterlingRingBoundCompanionsTakeNoArcheryWoundButStillTakeASkirmishWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetArcheryScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(sam, aragorn, gimli);

		var marksman = scn.GetShadowCard("marksman");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman, axeman);
		// Sam at 5 loses to the Axeman's 6 and lives through it; at his printed 3 he would be overwhelmed.
		scn.ApplyAdHocModifier(new StrengthModifier(null, sam, null, 2));
		scn.SkipToArcheryWounds();

		// The other [raider] Man is an Easterling: the archery wound is for the unbound companions alone.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, axeman}, new PhysicalCardImpl[]{gimli, marksman});
		scn.FreepsResolveSkirmish(sam);
		assertEquals(5, scn.GetStrength(sam));
		assertEquals(6, scn.GetStrength(axeman));
		scn.PassSkirmishActions();

		// A wound that is not an archery wound reaches a Ring-bound companion all the same.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
