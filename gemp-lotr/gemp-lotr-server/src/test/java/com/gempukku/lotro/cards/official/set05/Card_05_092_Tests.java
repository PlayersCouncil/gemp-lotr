package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_092_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sigewulf", "5_92");
					put("guard1", "5_83");      // Household Guard: [rohan] valiant Man companion, no text
					put("guard2", "5_83");
					put("guard3", "5_83");
					put("guard4", "5_83");
					put("aragorn", "102_16");   // Aragorn, Last Hope of Men: [gondor] valiant Man; a start-of-turn draw and a skirmish ability
					put("elf", "68_9");         // Elven Defender: [elven] valiant Elf companion; strength +2 only with an [elven] token
					put("rohirrim", "7_246");   // Rohirrim Guard: [rohan] Man companion who is not valiant; a skirmish ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion; its when-played text does nothing when cheated in
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion; damage +1 is its whole text
					put("chasm", "13_44");      // Chasm's Edge: Shadow condition, support area; pumps Gollum only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SigewulfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Sigewulf, Brave Volunteer
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 2
		 * Site Number: 4T
		 * Game Text: <b>Villager</b>.<br><b>Regroup:</b> Exert Sigewulf and spot 5 valiant Men to discard a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sigewulf");

		assertEquals("Sigewulf", card.getBlueprint().getTitle());
		assertEquals("Brave Volunteer", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VILLAGER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.TWO_TOWERS, 4)));
	}

	@Test
	public void RegroupAbilityExertsSigewulfWithFiveValiantMenToDiscardAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sigewulf = scn.GetFreepsCard("sigewulf");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var chasm = scn.GetShadowCard("chasm");
		scn.MoveAlliesToTable(sigewulf);

		scn.StartGame();
		// Aragorn goes down after the start of the turn, so that his start-of-turn draw is not asked.
		// Four [rohan] Men and a [gondor] Man make the five valiant Men.
		scn.MoveCompanionsToTable(guard1, guard2, guard3, guard4, aragorn);
		scn.MoveMinionsToTable(runner, savage);
		scn.MoveCardsToSupportArea(chasm);
		assertInPlay(runner, savage, chasm);

		// The minions are left unassigned and are still in play in the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(sigewulf));
		assertTrue(scn.FreepsActionAvailable(sigewulf));

		scn.FreepsUseCardAction(sigewulf);
		assertEquals(1, scn.GetWoundsOn(sigewulf));
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, savage));
		// Neither a Shadow condition nor a character who is no minion (a companion) can be discarded.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(chasm, guard1));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertInDiscard(savage);
		assertInPlay(runner, chasm);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithFourValiantMenBesideAValiantElfAndAManWhoIsNotValiant() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sigewulf = scn.GetFreepsCard("sigewulf");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elf = scn.GetFreepsCard("elf");
		var rohirrim = scn.GetFreepsCard("rohirrim");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveAlliesToTable(sigewulf);

		scn.StartGame();
		scn.MoveCompanionsToTable(guard1, guard2, guard3, aragorn, elf, rohirrim);
		scn.MoveMinionsToTable(runner, savage);
		assertInPlay(runner, savage);

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		// He could exert, and there are minions to discard: only the fifth valiant Man is missing.
		assertEquals(0, scn.GetWoundsOn(sigewulf));
		assertFalse(scn.FreepsActionAvailable(sigewulf));
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhenSigewulfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sigewulf = scn.GetFreepsCard("sigewulf");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveAlliesToTable(sigewulf);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(sigewulf, 1);

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(sigewulf));
		scn.MoveCompanionsToTable(guard1, guard2, guard3, guard4, aragorn);
		scn.MoveMinionsToTable(runner, savage);
		assertInPlay(runner, savage);

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(sigewulf));
		assertFalse(scn.FreepsActionAvailable(sigewulf));
	}

	@Test
	public void WithNoMinionInPlayRegroupAbilityIsStillOfferedAndSigewulfExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sigewulf = scn.GetFreepsCard("sigewulf");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");
		var aragorn = scn.GetFreepsCard("aragorn");
		var chasm = scn.GetShadowCard("chasm");
		scn.MoveAlliesToTable(sigewulf);

		scn.StartGame();
		scn.MoveCompanionsToTable(guard1, guard2, guard3, guard4, aragorn);
		scn.MoveCardsToSupportArea(chasm);
		assertInPlay(chasm);

		// No minions: the fellowship moves to site 2 and the game goes straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(sigewulf));
		// With a cost to pay, the ability is offered though there is no minion to discard (Q-0034).
		assertTrue(scn.FreepsActionAvailable(sigewulf));

		scn.FreepsUseCardAction(sigewulf);
		assertEquals(1, scn.GetWoundsOn(sigewulf));
		assertInPlay(chasm);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
