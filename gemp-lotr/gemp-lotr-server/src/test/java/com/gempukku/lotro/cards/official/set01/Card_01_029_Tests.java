package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_029_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enmity", "1_29");
					put("arwen", "1_30");
					put("gwemegil", "1_47");
					put("elrond", "1_40");

					put("uruk", "1_145");
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: Elf companion, strength 5 (7 at sites 6 to 8)
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AncientEnmityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Ancient Enmity
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make an Elf strength +1.
		 * 	If a minion loses this skirmish to that Elf, that minion's owner discards 2 cards at random from hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enmity");

		assertEquals("Ancient Enmity", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AncientEnmityLosingSkirmishDoesNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var arwen = scn.GetFreepsCard("arwen");
		var gwemegil = scn.GetFreepsCard("gwemegil");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(elrond);
		scn.AttachCardsTo(arwen, gwemegil);
		scn.MoveCardsToHand(enmity);

		var uruk = scn.GetShadowCard("uruk");
		scn.MoveMinionsToTable(uruk);
		scn.ShadowDrawCards(3);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, uruk);
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(6 + 2, scn.GetStrength(arwen));

		assertTrue(scn.FreepsPlayAvailable(enmity));
		scn.FreepsPlayCard(enmity);
		scn.FreepsChooseCard(arwen);
		assertEquals(6 + 2 + 1, scn.GetStrength(arwen));

		assertEquals(3, scn.GetShadowHandCount());
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(3, scn.GetShadowHandCount());
	}

	@Test
	public void AncientEnmityWinningSkirmishDiscards2CardsRandomlyFromShadowHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var arwen = scn.GetFreepsCard("arwen");
		var gwemegil = scn.GetFreepsCard("gwemegil");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(elrond);
		scn.AttachCardsTo(arwen, gwemegil);
		scn.MoveCardsToHand(enmity);

		var uruk = scn.GetShadowCard("uruk");
		scn.MoveMinionsToTable(uruk);
		scn.ShadowDrawCards(3);

		scn.StartGame();

		scn.AddWoundsToChar(elrond, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, uruk);
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(6 + 2, scn.GetStrength(arwen));

		assertTrue(scn.FreepsPlayAvailable(enmity));
		scn.FreepsPlayCard(enmity);
		scn.FreepsChooseCard(arwen);
		assertEquals(6 + 2 + 1, scn.GetStrength(arwen));

		scn.ShadowPass();
		scn.FreepsUseCardAction(gwemegil);
		assertEquals(6 + 2 + 1 + 1, scn.GetStrength(arwen));

		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		scn.ShadowPass();
		scn.FreepsPass();

		scn.FreepsResolveActionOrder("Ancient Enmity");
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(2 + 1, scn.GetShadowDiscardCount()); //2 hand cards + dead uruk
	}

	@Test
	public void TwoMinionsLosingTheOneSkirmishToThatElfDiscard2CardsOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var haldir = scn.GetFreepsCard("haldir");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(haldir);
		// Haldir, 5 + 5 and + 1 from the event, beats two Runners, 5 + 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, haldir, null, 5));
		scn.MoveCardsToHand(enmity);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.ShadowDrawCards(5);
		scn.SkipToAssignments();
		// The Shadow player sends the second Runner into the same skirmish.
		scn.FreepsAssignToMinions(haldir, runner);
		scn.ShadowAssignToMinions(haldir, runner2);
		scn.FreepsResolveSkirmish(haldir);
		// Haldir is the only Elf, so he is chosen without asking.
		scn.FreepsPlayCard(enmity);
		assertEquals(11, scn.GetStrength(haldir));

		assertEquals(5, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		scn.ShadowPass();
		scn.FreepsPass();

		// Ketura on Q-0113: "If a minion loses this skirmish to that Elf" is evaluated once, as "if at least 1
		// minion lost this skirmish to that Elf": both Runners are the Shadow player's, and he discards 2, once.
		scn.FreepsResolveActionOrder("Ancient Enmity");
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(2 + 2, scn.GetShadowDiscardCount()); // 2 hand cards + the two dead Runners
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
