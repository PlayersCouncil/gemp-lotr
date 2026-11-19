package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_249_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spires", "1_249");
					put("spires2", "1_249");

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; its text answers a weapon played on it
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					// Orc Ambusher: [sauron] Orc, strength 5, vitality 2; tracker, a roaming discount and a response to
					// a Free Peoples regroup event, none of which reaches these tests
					put("ambusher", "1_261");
					// Orc Assassin: [sauron] Orc, strength 6, vitality 2; tracker, a roaming discount and an assignment
					// action that needs 2 Hobbit companions, which these tables never have
					put("assassin", "1_262");
					// Cirith Ungol Sentinel: [sauron] Uruk-hai, a [sauron] minion who is no Orc, strength 7, vitality 2;
					// damage +1, and a discount on its own cost that does nothing in play
					put("sentinel", "10_83");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; cheated in, so its play trigger never fires

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man, strength 7, vitality 3; a skirmish ability used only on purpose
					// Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit), strength 3; enduring, and
					// read before he is wounded
					put("pippin", "103_31");
					put("guard", "5_83");       // Household Guard: [rohan] Man, strength 6; Valiant is its whole text
					// Gimli, Son of Gloin: [dwarven] Dwarf, strength 6, vitality 3, damage +1; "Skirmish: Exert Gimli to
					// make him strength +2"
					put("gimli", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spires", "1_249");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7
					// Gondorian Merchant: the one [gondor] ally, a Man, strength 6, vitality 1, home site 3 of the
					// Towers block; a maneuver ability used only on purpose
					put("merchant", "7_97");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GleamingSpiresWillCrumbleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Gleaming Spires Will Crumble
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>Each [sauron] Orc skirmishing a [gondor] character is strength +2.<br>Discard this condition if your [sauron] Orc loses a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spires");

		assertEquals("Gleaming Spires Will Crumble", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachSauronOrcSkirmishingAGondorCompanionIsStrengthPlusTwoOnlyWhileItSkirmishesHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var spires = scn.GetShadowCard("spires");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");
		var ambusher = scn.GetShadowCard("ambusher");
		scn.MoveCardsToSupportArea(spires);
		assertInZone(Zone.SUPPORT, spires);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, soldier, ambusher);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain}, new PhysicalCardImpl[]{boromir, ambusher});
		// The Orc left over is the Shadow player's to assign: two [sauron] Orcs in Aragorn's one skirmish.
		scn.ShadowAssignToMinions(aragorn, soldier);

		// Assigned, and no skirmish begun.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(5, scn.GetStrength(ambusher));

		// Boromir's skirmish first: the Orcs assigned to Aragorn are not skirmishing yet.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(ambusher));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(7, scn.GetStrength(soldier));

		// 7 against Boromir's 7: the Orc wins, and nothing is discarded.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertInZone(Zone.SUPPORT, spires);

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(chieftain));
		assertEquals(9, scn.GetStrength(soldier));
		// Its skirmish over, the Ambusher is skirmishing nobody.
		assertEquals(5, scn.GetStrength(ambusher));
	}

	@Test
	public void NoBonusAgainstACharacterOfAnotherCultureOrForAMinionThatIsNoSauronOrcAndNoDiscardWhenSuchAMinionLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(guard, aragorn, boromir, pippin);

		var spires = scn.GetShadowCard("spires");
		var chieftain = scn.GetShadowCard("chieftain");
		var sentinel = scn.GetShadowCard("sentinel");
		var runner = scn.GetShadowCard("runner");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(spires);
		assertInZone(Zone.SUPPORT, spires);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, sentinel, runner, soldier);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, chieftain}, new PhysicalCardImpl[]{aragorn, sentinel},
				new PhysicalCardImpl[]{boromir, runner}, new PhysicalCardImpl[]{pippin, soldier});

		// A [sauron] Orc skirmishing a [rohan] Man.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(chieftain));
		scn.FreepsPass();
		scn.ShadowPass();

		// A [sauron] Uruk-hai skirmishing a [gondor] Man: no bonus, and 7 against 8 it loses.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(sentinel));
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(sentinel));
		assertInZone(Zone.SUPPORT, spires);

		// A [moria] Orc skirmishing a [gondor] Man: no bonus, and 5 against 7 it loses (and dies).
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(runner));
		scn.FreepsPass();
		scn.ShadowPass();
		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.SUPPORT, spires);

		// A [sauron] Orc skirmishing a [gondor] companion who is no Man.
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(9, scn.GetStrength(soldier));
	}

	@Test
	public void ASauronOrcSkirmishingTheGondorAllyAtHisHomeSiteIsStrengthPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveAlliesToTable(merchant);

		var spires = scn.GetShadowCard("spires");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(spires);
		assertInZone(Zone.SUPPORT, spires);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(chieftain);
		// The fellowship moves on to site 3, the Merchant's home, where he may be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merchant, chieftain);

		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(7, scn.GetStrength(chieftain));
		scn.FreepsResolveSkirmish(merchant);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(chieftain));
	}

	@Test
	public void StaysWhenASauronOrcWinsAndIsDiscardedWhenOneLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);

		var spires = scn.GetShadowCard("spires");
		var chieftain = scn.GetShadowCard("chieftain");
		var ambusher = scn.GetShadowCard("ambusher");
		scn.MoveCardsToSupportArea(spires);
		assertInZone(Zone.SUPPORT, spires);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, ambusher);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, chieftain}, new PhysicalCardImpl[]{aragorn, ambusher});

		// 7 against Gimli's 6: the [sauron] Orc wins, and the condition stays.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(7, scn.GetStrength(chieftain));
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SUPPORT, spires);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// 5 + 2 = 7 against Aragorn's 8: the [sauron] Orc loses, lives through it, and the condition goes.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(7, scn.GetStrength(ambusher));
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveRuleFirst();

		assertEquals(1, scn.GetWoundsOn(ambusher));
		assertInZone(Zone.SHADOW_CHARACTERS, ambusher);
		assertInZone(Zone.DISCARD, spires);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachCopyIsDiscardedWhenASauronOrcIsKilledLosingToACharacterOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var spires = scn.GetShadowCard("spires");
		var spires2 = scn.GetShadowCard("spires2");
		var assassin = scn.GetShadowCard("assassin");
		scn.MoveCardsToSupportArea(spires, spires2);
		assertInZone(Zone.SUPPORT, spires, spires2);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, assassin);
		scn.FreepsResolveSkirmish(gimli);

		// Gimli exerts to be 8 against the Assassin's 6, and his damage +1 kills it: two wounds on vitality 2.
		assertEquals(6, scn.GetStrength(assassin));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(8, scn.GetStrength(gimli));
		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveRuleFirst();

		// Each copy discards itself, and nothing more is asked.
		assertInZone(Zone.DISCARD, assassin);
		assertInZone(Zone.DISCARD, spires, spires2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
