package com.gempukku.lotro.cards.official.set00;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_00_056_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ghan", "0_56");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetOverwhelmScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ghan", "0_56");
					put("knight", "5_35");      // Gondorian Knight: a Man companion, strength 6; "Knight" is his whole text
					put("merchant", "7_97");    // Gondorian Merchant: a Man ally; a maneuver ability that needs initiative, used only on purpose
					put("brigand", "4_10");     // Dunlending Brigand: a Man minion of the opponent's, no text
					put("thrarin", "1_27");     // Thrarin: a Dwarf ally, no Man; a maneuver ability used only on purpose
					put("troop1", "1_177");     // Goblin Patrol Troop: an Orc minion of strength 13, no text
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetForestScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ghan", "0_56");
					put("knight", "5_35");      // Gondorian Knight: another companion, who goes where Ghan goes
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_330");      // Derndingle: Forest. Move limit +1 while an unbound Hobbit is there; none is
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GhanburiGhanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 0
		 * Name: Ghân-buri-Ghân, Chieftain of the Woses
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: While you can spot 2 other Men, Ghân-buri-Ghân cannot be overwhelmed unless his strength is tripled.<br>While at a forest site, Ghân-buri-Ghân is <b>defender +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ghan");

		assertEquals("Ghân-buri-Ghân", card.getBlueprint().getTitle());
		assertEquals("Chieftain of the Woses", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	@Test
	public void WithTwoOtherMenHeIsNotOverwhelmedAtDoubleStrengthAndAnotherManIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOverwhelmScenario();

		var ghan = scn.GetFreepsCard("ghan");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(ghan, knight);
		scn.MoveAlliesToTable(merchant);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		// 13 + 4 = 17: more than double Ghan's 6, one short of triple.
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop1, null, 4));
		scn.SkipToAssignments();
		// Ghan spots the Knight and the Merchant; the Knight spots Ghan and the Merchant.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{ghan, troop1}, new PhysicalCardImpl[]{knight, troop2});

		scn.FreepsResolveSkirmish(ghan);
		assertEquals(6, scn.GetStrength(ghan));
		assertEquals(17, scn.GetStrength(troop1));
		assertEquals(0, scn.GetWoundsOn(ghan));
		scn.PassSkirmishActions();
		// He loses and takes the 1 wound for losing.
		assertEquals(1, scn.GetWoundsOn(ghan));
		assertInPlay(ghan);

		// The Knight, at 6 against 13, is overwhelmed.
		scn.FreepsResolveSkirmish(knight);
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(13, scn.GetStrength(troop2));
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, knight);
		assertInPlay(ghan);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyAndAnOpponentsManMinionAreTwoOtherMen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOverwhelmScenario();

		var ghan = scn.GetFreepsCard("ghan");
		var merchant = scn.GetFreepsCard("merchant");
		var brigand = scn.GetShadowCard("brigand");
		var troop = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(ghan);
		scn.MoveAlliesToTable(merchant);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, brigand);
		// 13 + 4 = 17: more than double Ghan's 6, one short of triple.
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop, null, 4));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(ghan, troop);
		// The Brigand is left unassigned, and stays in play to be spotted.
		scn.ShadowDeclineAssignments();
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);

		scn.FreepsResolveSkirmish(ghan);
		assertEquals(17, scn.GetStrength(troop));
		assertEquals(0, scn.GetWoundsOn(ghan));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(ghan));
		assertInPlay(ghan);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOnlyOneOtherManHeIsOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOverwhelmScenario();

		var ghan = scn.GetFreepsCard("ghan");
		var knight = scn.GetFreepsCard("knight");
		var thrarin = scn.GetFreepsCard("thrarin");
		var troop = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(ghan, knight);
		// Beside the Knight: Frodo, a Dwarf ally and an Orc minion, none of them a Man.
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(ghan, troop);

		scn.FreepsResolveSkirmish(ghan);
		assertEquals(6, scn.GetStrength(ghan));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.PassSkirmishActions();
		// Vitality 3: the wound for losing would not kill him; the overwhelm does.
		assertInZone(Zone.DEAD, ghan);
		assertInPlay(knight);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTwoOtherMenHeIsOverwhelmedOnceHisStrengthIsTripled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOverwhelmScenario();

		var ghan = scn.GetFreepsCard("ghan");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var troop = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(ghan, knight);
		scn.MoveAlliesToTable(merchant);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// Fierce, so that Ghan fights it twice; 13 + 4 = 17, one short of triple his 6.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop, null, Keyword.FIERCE));
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop, null, 4));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(ghan, troop);
		scn.FreepsResolveSkirmish(ghan);
		assertEquals(17, scn.GetStrength(troop));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(ghan));
		assertInPlay(ghan);

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(ghan, troop);
		// One more: 18, exactly triple.
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop, null, 1));
		scn.FreepsResolveSkirmish(ghan);
		assertEquals(18, scn.GetStrength(troop));
		assertEquals(6, scn.GetStrength(ghan));
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.PassFierceSkirmishActions();

		// Vitality 3 with 1 wound: the wound for losing would not kill him; the overwhelm does.
		assertInZone(Zone.DEAD, ghan);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtAForestSiteHeIsDefenderPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetForestScenario();

		var ghan = scn.GetFreepsCard("ghan");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(ghan, knight);

		scn.StartGame();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertFalse(scn.HasKeyword(ghan, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(knight, Keyword.DEFENDER));

		// The fellowship moves to Derndingle.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertTrue(scn.HasKeyword(ghan, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(ghan, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(knight, Keyword.DEFENDER));

		// And on to the Barrows of Edoras.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertFalse(scn.HasKeyword(ghan, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(knight, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
