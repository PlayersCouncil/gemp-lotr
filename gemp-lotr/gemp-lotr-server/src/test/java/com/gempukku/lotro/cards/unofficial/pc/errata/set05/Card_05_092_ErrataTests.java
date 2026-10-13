package com.gempukku.lotro.cards.unofficial.pc.errata.set05;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_092_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sigewulf", "55_92");
					put("guard1", "5_83");      // Household Guard: [rohan] valiant Man companion, no text
					put("guard2", "5_83");
					put("guard3", "5_83");
					put("guard4", "5_83");
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
		 * Game Text: <b>Villager</b>.<br><b>Regroup:</b> Exert Sigewulf and spot 5 valiant companions to discard a minion.
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
	public void PlayedToTheSupportAreaAndInTheRegroupPhaseExertsToDiscardAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sigewulf = scn.GetFreepsCard("sigewulf");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");
		var elf = scn.GetFreepsCard("elf");
		var rohirrim = scn.GetFreepsCard("rohirrim");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var chasm = scn.GetShadowCard("chasm");
		scn.MoveCompanionsToTable(guard1, guard2, guard3, guard4, elf, rohirrim);
		scn.MoveCardsToHand(sigewulf);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(sigewulf));
		scn.FreepsPlayCard(sigewulf);
		assertInZone(Zone.SUPPORT, sigewulf);

		// No minions: the fellowship moves to site 2 and the game goes straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		// With a cost to pay, the ability is offered though there is no minion to discard.
		assertTrue(scn.FreepsActionAvailable(sigewulf));

		scn.MoveMinionsToTable(runner, savage);
		scn.MoveCardsToSupportArea(chasm);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(sigewulf));
		// Four [rohan] Men and an Elf make the five valiant companions.
		assertTrue(scn.FreepsActionAvailable(sigewulf));

		scn.FreepsUseCardAction(sigewulf);
		assertEquals(1, scn.GetWoundsOn(sigewulf));
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, savage));
		// Neither the Shadow condition nor a character who is no minion (a companion) can be discarded.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(chasm, guard1));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertInDiscard(savage);
		assertInPlay(runner, chasm);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithFourValiantCompanionsAndARohanCompanionWhoIsNotValiant() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sigewulf = scn.GetFreepsCard("sigewulf");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var elf = scn.GetFreepsCard("elf");
		var rohirrim = scn.GetFreepsCard("rohirrim");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(guard1, guard2, guard3, elf, rohirrim);
		scn.MoveAlliesToTable(sigewulf);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(runner, savage);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(sigewulf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sigewulf));
	}

	@Test
	public void NotOfferedWhenSigewulfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sigewulf = scn.GetFreepsCard("sigewulf");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");
		var elf = scn.GetFreepsCard("elf");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(guard1, guard2, guard3, guard4, elf);
		scn.MoveAlliesToTable(sigewulf);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(sigewulf, 1);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(runner, savage);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetWoundsOn(sigewulf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sigewulf));
	}
}
