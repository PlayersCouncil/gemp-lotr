package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_061_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attea", "103_61");
					put("attacker", "7_141");   // Easterling Attacker: [raider] Man, Easterling, strength 6; a skirmish ability never used here
					put("guard", "4_226");      // Easterling Guard: [raider] Man, Easterling, strength 9, vitality 2; assigned to an unbound companion, may exert to add a burden
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text at all
					put("soldier", "4_254");    // Southron Soldier: [raider] Man, Southron, strength 4, ambush (2), no other text
					put("lemenya", "1_232");    // Ulaire Lemenya: [wraith] Nazgul, strength 9; weakens bearers of [wraith] conditions, of which there are none
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireAtteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ulaire Attea, Khamul of the East
		 * Unique: true
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Easterling.</b>  Fierce.
		 * 	While you can spot 3 burdens, each Easterling is strength +3.
		 * 	While you can spot 5 burdens, each Easterling is <b>damage +1</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("attea");

		assertEquals("Ulaire Attea", card.getBlueprint().getTitle());
		assertEquals("Khamul of the East", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.EASTERLING));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheThirdBurdenMakesEachEasterlingStrengthPlusThreeAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var attacker = scn.GetShadowCard("attacker");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(attea, attacker, guard, troop, soldier, lemenya);
		scn.AddBurdens(2);
		scn.SkipToAssignments();

		// 2 burdens: one short.
		assertEquals(2, scn.GetBurdens());
		assertEquals(12, scn.GetStrength(attea));
		assertEquals(6, scn.GetStrength(attacker));
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(4, scn.GetStrength(soldier));
		assertEquals(9, scn.GetStrength(lemenya));

		// The Guard, assigned to an unbound companion, exerts to add the third burden.
		scn.FreepsAssignToMinions(aragorn, guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(guard));
		scn.ShadowChooseOptionalTrigger(guard);

		assertEquals(3, scn.GetBurdens());
		// Attea himself is an Easterling, and the one who is no Man: 12 + 3
		assertEquals(15, scn.GetStrength(attea));
		// 6 + 3
		assertEquals(9, scn.GetStrength(attacker));
		// 9 + 3
		assertEquals(12, scn.GetStrength(guard));
		// A [raider] Man who is a Southron, a Nazgul who is no Easterling and an Orc gain nothing.
		assertEquals(4, scn.GetStrength(soldier));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(13, scn.GetStrength(troop));
		// 3 burdens are not 5: nobody is damage +1.
		assertEquals(0, scn.GetKeywordCount(attea, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(attacker, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertTrue(scn.ShadowDecisionAvailable("assign"));
	}

	@Test
	public void TheFifthBurdenMakesEachEasterlingDamagePlusOneAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var attacker = scn.GetShadowCard("attacker");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(attea, attacker, guard, troop, soldier, lemenya);
		scn.AddBurdens(4);
		scn.SkipToAssignments();

		// 4 burdens: one short for the damage bonus, while the strength bonus is in force.
		assertEquals(4, scn.GetBurdens());
		assertEquals(0, scn.GetKeywordCount(attea, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(attacker, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(soldier, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		// 12 + 3, 6 + 3
		assertEquals(15, scn.GetStrength(attea));
		assertEquals(9, scn.GetStrength(attacker));

		// The Guard, assigned to an unbound companion, exerts to add the fifth burden.
		scn.FreepsAssignToMinions(aragorn, guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(guard));
		scn.ShadowChooseOptionalTrigger(guard);

		assertEquals(5, scn.GetBurdens());
		assertEquals(1, scn.GetKeywordCount(attea, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(attacker, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		// A [raider] Man who is a Southron, a Nazgul who is no Easterling and an Orc gain nothing.
		assertEquals(0, scn.GetKeywordCount(soldier, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		// The strength bonus is 3 whatever the number of burdens: 12 + 3, 6 + 3, 9 + 3
		assertEquals(15, scn.GetStrength(attea));
		assertEquals(9, scn.GetStrength(attacker));
		assertEquals(12, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(soldier));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.ShadowDecisionAvailable("assign"));
	}
}
