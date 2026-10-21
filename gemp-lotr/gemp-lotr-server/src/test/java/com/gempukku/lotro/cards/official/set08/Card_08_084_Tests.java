package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_084_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wk", "8_84");
					put("theoden", "4_292");    // Théoden, Son of Thengel: [rohan] Man companion, strength 6, vitality 2; a regroup ability only
					// Herugrim (the PC erratum): a [rohan] ARTIFACT hand weapon, strength +2, vitality +1, on Théoden; damage +1
					put("herugrim", "54_280");
					// Rohirrim Bow: a [rohan] ranged weapon, a possession, strength +1; an archery ability used only on purpose
					put("bow", "5_88");
					// Firefoot, Mearas of The Mark: a [rohan] mount, strength +1, no weapon; its other text is for Éomer and
					// for its own play
					put("firefoot", "13_126");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, vitality 3; Knight only
					// Gondorian Sword: [gondor] hand weapon, strength +2; a skirmish ability used only on purpose
					put("gsword", "7_98");
					put("gimli", "1_13");       // Gimli, Son of Glóin: a Dwarf companion, strength 6; a skirmish ability used only on purpose
					// Dwarven Axe: [dwarven] hand weapon, strength +2; its trigger answers a minion losing to bearer
					put("axe", "1_9");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path, on which the Gondorian Merchant (home site 3) skirmishes at site 3 as an ally
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wk", "8_84");
					// Gondorian Merchant: [gondor] Man ally, strength 6, vitality 1, home site 3 of the Towers block; a
					// maneuver ability used only on purpose
					put("merchant", "7_97");
					// Sword of Gondor: [gondor] hand weapon, strength +2; damage +1 only against a Man or a roaming minion
					put("ssword", "4_134");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWitchkingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: The Witch-king, Black Captain
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 14
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Enduring</b>.<br>Each Man skirmishing The Witch-king loses all strength bonuses from weapons.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wk");

		assertEquals("The Witch-king", card.getBlueprint().getTitle());
		assertEquals("Black Captain", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AManSkirmishingTheWitchkingLosesTheBonusOfEachWeaponAndKeepsHisMountsWhileAManSkirmishingAnotherMinionKeepsHis() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var herugrim = scn.GetFreepsCard("herugrim");
		var bow = scn.GetFreepsCard("bow");
		var firefoot = scn.GetFreepsCard("firefoot");
		var knight = scn.GetFreepsCard("knight");
		var gsword = scn.GetFreepsCard("gsword");
		scn.MoveCompanionsToTable(theoden, knight);
		scn.AttachCardsTo(theoden, herugrim, bow, firefoot);
		scn.AttachCardsTo(knight, gsword);

		var wk = scn.GetShadowCard("wk");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(wk, troop);

		// 6 + 2 Herugrim + 1 Bow + 1 Firefoot; 6 + 2 Gondorian Sword.
		assertEquals(10, scn.GetStrength(theoden));
		assertEquals(8, scn.GetStrength(knight));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{theoden, wk}, new PhysicalCardImpl[]{knight, troop});

		// Assigned to the Witch-king, Théoden is not yet skirmishing him: his weapons count.
		assertEquals(10, scn.GetStrength(theoden));

		// The Knight skirmishing the Troop keeps his sword's bonus.
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.IsCharSkirmishingAgainst(knight, troop));
		assertEquals(8, scn.GetStrength(knight));
		assertEquals(10, scn.GetStrength(theoden));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Skirmishing the Witch-king, Théoden loses the bonus of both weapons, the artifact's and the possession's,
		// and keeps his mount's: 6 + 1 Firefoot.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(theoden);
		assertTrue(scn.IsCharSkirmishingAgainst(theoden, wk));
		assertEquals(7, scn.GetStrength(theoden));
		assertEquals(8, scn.GetStrength(knight));
		assertEquals(14, scn.GetStrength(wk));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ADwarfSkirmishingTheWitchkingKeepsHisWeaponBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);

		var wk = scn.GetShadowCard("wk");

		scn.StartGame();
		scn.MoveMinionsToTable(wk);

		// 6 + 2 Dwarven Axe.
		assertEquals(8, scn.GetStrength(gimli));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, wk);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.IsCharSkirmishingAgainst(gimli, wk));
		assertEquals(8, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AManAllySkirmishingTheWitchkingLosesHisWeaponBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var merchant = scn.GetFreepsCard("merchant");
		var ssword = scn.GetFreepsCard("ssword");
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, ssword);

		var wk = scn.GetShadowCard("wk");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(wk);

		// 6 + 2 Sword of Gondor.
		assertEquals(8, scn.GetStrength(merchant));

		// The move to site 3, the Merchant's home.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merchant, wk);
		assertEquals(8, scn.GetStrength(merchant));
		scn.FreepsResolveSkirmish(merchant);

		assertTrue(scn.IsCharSkirmishingAgainst(merchant, wk));
		assertEquals(6, scn.GetStrength(merchant));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
