package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_011_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "15_11");
					// Aragorn, Ranger of the North: [gondor] Aragorn; a Maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Aragorn, Defender of Rohan: the one Aragorn of another culture; his text needs the Ring-bearer assigned
					put("rohanaragorn", "17_93");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] companion who is no Aragorn; a Skirmish ability used only on purpose
					put("lorien", "1_53");      // Lórien Elf: another Elf companion, strength 4, no other text
					// Merry, From O'er the Brandywine: [shire] Hobbit companion, strength 3, vitality 4; his text needs a weapon
					put("merry", "1_303");

					// The cards that carry the tokens, each given tokens of its own culture as it is played (cheated in here):
					// Strands of Elven Hair ([elven] support possession) and Suspended Palaces ([dwarven] support
					// condition) have Fellowship abilities only.
					put("strands", "9_22");
					put("palaces", "6_10");

					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, strength 5, vitality 3; a Regroup ability used only on purpose
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, vitality 2, damage +1
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArwenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Arwen, She-Elf
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: While you can spot Aragorn, Arwen is strength +1 and resistance +2.<br><b>Skirmish:</b> Remove an [elven] token to make a minion skirmishing Arwen strength -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arwen");

		assertEquals("Arwen", card.getBlueprint().getTitle());
		assertEquals("She-Elf", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void OnceAragornIsPlayedArwenIsStrengthPlus1AndResistancePlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var lorien = scn.GetFreepsCard("lorien");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, lorien);
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();

		assertInHand(aragorn);
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(6, scn.GetResistance(arwen));
		assertEquals(4, scn.GetStrength(lorien));
		assertEquals(6, scn.GetResistance(lorien));

		scn.FreepsPlayCard(aragorn);

		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(7, scn.GetStrength(arwen));
		assertEquals(8, scn.GetResistance(arwen));
		// Another Elf companion is not touched.
		assertEquals(4, scn.GetStrength(lorien));
		assertEquals(6, scn.GetResistance(lorien));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABoromirDoesNotStrengthenHerButTheRohanAragornDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var rohanaragorn = scn.GetFreepsCard("rohanaragorn");
		scn.MoveCompanionsToTable(arwen, boromir);

		scn.StartGame();

		// A [gondor] companion who is no Aragorn.
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(6, scn.GetResistance(arwen));

		// The [rohan] Aragorn is an Aragorn all the same.
		scn.MoveCompanionsToTable(rohanaragorn);
		assertEquals(7, scn.GetStrength(arwen));
		assertEquals(8, scn.GetResistance(arwen));
	}

	@Test
	public void ElvenTokenMakesAMinionSkirmishingArwenStrengthMinus2UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var merry = scn.GetFreepsCard("merry");
		var strands = scn.GetFreepsCard("strands");
		var worker = scn.GetShadowCard("worker");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen, merry);
		scn.MoveCardsToSupportArea(strands);

		scn.StartGame();
		scn.AddTokensToCard(strands, 2);
		assertEquals(2, scn.GetCultureTokensOn(strands));

		scn.MoveMinionsToTable(worker, sentry, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, worker}, new PhysicalCardImpl[]{merry, troop});
		scn.ShadowAssignToMinions(arwen, sentry);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(5, scn.GetStrength(worker));
		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.FreepsActionAvailable(arwen));
		scn.FreepsUseCardAction(arwen);

		assertEquals(1, scn.GetCultureTokensOn(strands));
		// The two minions skirmishing Arwen, of two cultures and two races; not the Troop, assigned to Merry.
		assertTrue(scn.FreepsHasCardChoiceAvailable(worker, sentry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(worker);

		assertEquals(3, scn.GetStrength(worker));
		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still so at the next decision of the same skirmish.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(worker));

		// 6 against 3 + 8: Arwen loses, and takes 2 wounds for the Sentry's damage +1.  The minions live on.
		scn.FreepsPass();
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertInZone(Zone.SHADOW_CHARACTERS, worker);

		// Each skirmish is a phase of its own: in Merry's, the Worker is back to his printed strength.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(worker));
		assertEquals(8, scn.GetStrength(sentry));
	}

	@Test
	public void ElvenTokenAbilityUsedInAnotherSkirmishReachesNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var merry = scn.GetFreepsCard("merry");
		var strands = scn.GetFreepsCard("strands");
		var worker = scn.GetShadowCard("worker");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen, merry);
		scn.MoveCardsToSupportArea(strands);

		scn.StartGame();
		scn.AddTokensToCard(strands, 2);
		assertEquals(2, scn.GetCultureTokensOn(strands));

		scn.MoveMinionsToTable(worker, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, worker}, new PhysicalCardImpl[]{merry, troop});
		// Merry's skirmish first: the Worker is assigned to Arwen, but not yet skirmishing her.
		scn.FreepsResolveSkirmish(merry);

		assertEquals(5, scn.GetStrength(worker));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.FreepsActionAvailable(arwen));

		// The ability has a cost, so it is offered: the token is paid, and no minion is skirmishing Arwen.
		scn.FreepsUseCardAction(arwen);
		assertEquals(1, scn.GetCultureTokensOn(strands));
		assertEquals(5, scn.GetStrength(worker));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoElvenTokenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var palaces = scn.GetFreepsCard("palaces");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(palaces);

		scn.StartGame();
		// Tokens of another culture only.
		scn.AddTokensToCard(palaces, 2);
		assertEquals(2, scn.GetCultureTokensOn(palaces));

		scn.MoveMinionsToTable(worker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, worker);
		scn.FreepsResolveSkirmish(arwen);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(arwen));
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		assertEquals(5, scn.GetStrength(worker));
	}
}
