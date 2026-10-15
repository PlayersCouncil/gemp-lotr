package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_064_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("commander", "3_64");
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, strength 6; a Shadow ability used only on purpose
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, strength 7; a Regroup ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, Damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion; a maneuver ability used only on purpose
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: an ally; a fellowship ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcCommanderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Orc Commander
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 11
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: Each other [isengard] Orc is strength +1.<br><b>Regroup:</b> Exert Orc Commander to make the Free Peoples player wound a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("commander");

		assertEquals("Orc Commander", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void OncePlayedEachOtherIsengardOrcIsStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander = scn.GetShadowCard("commander");
		var forger = scn.GetShadowCard("forger");
		var servant = scn.GetShadowCard("servant");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(commander);

		scn.StartGame();
		scn.SetTwilight(20);
		// Minions laid early, to read the standing text on them before and after the Commander is played (Q-0228).
		scn.MoveMinionsToTable(forger, servant, savage, troop);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(6, scn.GetStrength(forger));
		assertEquals(7, scn.GetStrength(servant));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));

		scn.ShadowPlayCard(commander);
		assertInZone(Zone.SHADOW_CHARACTERS, commander);

		// Two [isengard] Orcs of different titles: +1 each.
		assertEquals(7, scn.GetStrength(forger));
		assertEquals(8, scn.GetStrength(servant));
		// An [isengard] Uruk-hai and a [moria] Orc: unchanged.
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
		// "Other": not the Commander himself.
		assertEquals(11, scn.GetStrength(commander));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsHimToMakeTheFreePeoplesPlayerWoundACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander = scn.GetShadowCard("commander");
		var forger = scn.GetShadowCard("forger");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(maggot);

		scn.StartGame();
		scn.MoveMinionsToTable(commander, forger);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(commander));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(maggot));
		assertEquals(0, scn.GetWoundsOn(forger));
		assertTrue(scn.ShadowActionAvailable(commander));

		scn.ShadowUseCardAction(commander);
		assertEquals(1, scn.GetWoundsOn(commander));

		// The Free Peoples player chooses: a companion, the Ring-bearer among them; not an ally, not a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(maggot, forger, commander));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(maggot));
		assertEquals(0, scn.GetWoundsOn(forger));
		assertEquals(1, scn.GetWoundsOn(commander));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedOnceHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander = scn.GetShadowCard("commander");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(commander);
		// Vitality 4 with 3 wounds: he cannot exert.
		scn.AddWoundsToChar(commander, 3);
		assertEquals(3, scn.GetWoundsOn(commander));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(3, scn.GetWoundsOn(commander));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(commander));
	}
}
