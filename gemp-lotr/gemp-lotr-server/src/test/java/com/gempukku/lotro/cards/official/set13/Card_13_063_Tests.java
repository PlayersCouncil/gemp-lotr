package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_063_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brego", "13_63");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, Knight and no other text
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; a skirmish ability only
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] ally; a maneuver ability used only on purpose
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, Valiant and no other text
					// Garrison of Osgiliath and Heirs of Gondor: [gondor] support-area conditions that take [gondor]
					// tokens as they are played (cheated in they have none); skirmish abilities only
					put("garrison", "6_52");
					put("heirs", "13_69");
					// A Path Appointed: [gondor] support-area condition that adds [gondor] tokens to itself by its
					// fellowship ability (used only on purpose); laid with none
					put("path", "8_42");
					// Wish For Our Kinfolk: [dwarven] support-area condition that adds [dwarven] tokens to itself
					// by its fellowship ability (used only on purpose)
					put("wish", "8_8");
					put("shield", "1_107");     // Great Shield: [gondor] possession, "Bearer must be a Man"; archery total -1
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc of vitality 1; a play trigger only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetReinforceScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brego", "13_63");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion to bear Brego
					// Two [gondor] conditions of the support area that carry [gondor] tokens; each has a
					// skirmish ability, never used.
					put("leaders", "7_112");    // Noble Leaders
					put("heirs", "13_69");      // Heirs of Gondor
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BregoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Brego, Loyal Steed
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Game Text: Bearer must be a [gondor] companion.<br>When you play Brego, you may reinforce a [gondor] token.<br><b>Maneuver:</b> Exert bearer and remove 2 [gondor] tokens to wound a minion.
		*/

		var scn = GetScenario();

		var brego = scn.GetFreepsCard("brego");

		assertEquals("Brego", brego.getBlueprint().getTitle());
		assertEquals("Loyal Steed", brego.getBlueprint().getSubtitle());
		assertTrue(brego.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, brego.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, brego.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, brego.getBlueprint().getCardType());
		assertTrue(brego.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, brego.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedOnAGondorCompanionItMayReinforceAGondorTokenOnACardThatHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brego = scn.GetFreepsCard("brego");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var guard = scn.GetFreepsCard("guard");
		var garrison = scn.GetFreepsCard("garrison");
		var heirs = scn.GetFreepsCard("heirs");
		var path = scn.GetFreepsCard("path");
		var wish = scn.GetFreepsCard("wish");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(knight, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(garrison, heirs, path, wish);
		scn.AddTokensToCard(garrison, 2);
		scn.AddTokensToCard(heirs, 1);
		scn.AddTokensToCard(wish, 1);
		scn.MoveCardsToHand(brego);

		scn.StartGame();

		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertEquals(0, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetCultureTokensOn(wish));
		scn.FreepsPlayCard(brego);
		// A [gondor] Man companion and a [gondor] Hobbit companion may bear it; not the [gondor] ally, the [rohan]
		// companion or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);
		assertAttachedTo(brego, pippin);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brego));
		scn.FreepsAcceptOptionalTrigger();
		// Either [gondor] card that has a [gondor] token; not the [gondor] card with none, nor the card with a
		// [dwarven] token.
		assertTrue(scn.FreepsHasCardChoiceAvailable(garrison, heirs));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(path, wish));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(heirs);
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(2, scn.GetCultureTokensOn(heirs));
		assertEquals(0, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetCultureTokensOn(wish));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheReinforcementMayBeDeclinedAndAnotherGondorPossessionPlayedAfterBregoIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brego = scn.GetFreepsCard("brego");
		var knight = scn.GetFreepsCard("knight");
		var shield = scn.GetFreepsCard("shield");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(garrison);
		scn.AddTokensToCard(garrison, 1);
		scn.MoveCardsToHand(brego, shield);

		scn.StartGame();

		assertEquals(1, scn.GetCultureTokensOn(garrison));
		// The Knight is the only [gondor] companion, so Brego goes on him unasked.
		scn.FreepsPlayCard(brego);
		assertAttachedTo(brego, knight);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brego));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another [gondor] possession played after Brego, with the token still there to reinforce.
		scn.FreepsPlayCard(shield);
		assertAttachedTo(shield, knight);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(brego));
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOnlyADwarvenTokenInPlayTheReinforcementIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brego = scn.GetFreepsCard("brego");
		var knight = scn.GetFreepsCard("knight");
		var path = scn.GetFreepsCard("path");
		var wish = scn.GetFreepsCard("wish");
		scn.MoveCompanionsToTable(knight);
		// A [gondor] card with no token, and a [dwarven] token on a [dwarven] card.
		scn.MoveCardsToSupportArea(path, wish);
		scn.AddTokensToCard(wish, 2);
		scn.MoveCardsToHand(brego);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(path));
		assertEquals(2, scn.GetCultureTokensOn(wish));
		scn.FreepsPlayCard(brego);
		assertAttachedTo(brego, knight);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(brego));
		assertEquals(0, scn.GetCultureTokensOn(path));
		assertEquals(2, scn.GetCultureTokensOn(wish));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Ketura on Q-0034: an optional action that has no cost and would do nothing is not offered.  Brego's
	// reinforcing has no cost, and a token on a hindered card cannot be reinforced (CR 5.0 "hinder"), so with the
	// only [gondor] token on a hindered card there is nothing for it to do.  The old at/ test
	// (HinderTests.CannotReinforceCultureTokenOnHinderedCard) took the offer on this board and read the token
	// count unchanged; the definition was corrected for this test (F-0359).  The offer made, with tokens that
	// can be reinforced beside the hindered one, is read in
	// mechanics/hinder/TokensOnAHinderedCardTests.NoCultureTokenIsReinforcedOnAHinderedCard.
	@Test
	public void ReinforcingIsNotOfferedWhenTheOnlyGondorTokenIsOnAHinderedCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetReinforceScenario();

		var brego = scn.GetFreepsCard("brego");
		var aragorn = scn.GetFreepsCard("aragorn");
		var leaders = scn.GetFreepsCard("leaders");
		var heirs = scn.GetFreepsCard("heirs");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(leaders, heirs);
		scn.MoveCardsToHand(brego);

		scn.StartGame();
		// Heirs of Gondor has no token on it; Noble Leaders has the only one, and is hindered.
		scn.AddTokensToCard(leaders, 1);
		scn.HinderCard(leaders);

		assertEquals(1, scn.GetCultureTokensOn(leaders));
		assertEquals(0, scn.GetCultureTokensOn(heirs));
		scn.FreepsPlayCard(brego);
		assertAttachedTo(brego, aragorn);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(brego));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetCultureTokensOn(leaders));
		assertEquals(0, scn.GetCultureTokensOn(heirs));
	}

	@Test
	public void ManeuverAbilityExertsBearerAndRemovesTwoGondorTokensToWoundAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brego = scn.GetFreepsCard("brego");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var garrison = scn.GetFreepsCard("garrison");
		var heirs = scn.GetFreepsCard("heirs");
		var wish = scn.GetFreepsCard("wish");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(knight, pippin);
		scn.AttachCardsTo(knight, brego);
		scn.MoveCardsToSupportArea(garrison, heirs, wish);
		scn.AddTokensToCard(garrison, 1);
		scn.AddTokensToCard(heirs, 1);
		scn.AddTokensToCard(wish, 1);

		scn.StartGame();
		assertAttachedTo(brego, knight);
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(wish));
		scn.MoveMinionsToTable(troop, savage, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.FreepsActionAvailable(brego));
		scn.FreepsUseCardAction(brego);
		// The two tokens come one card at a time: the [gondor] cards, never the [dwarven] one; the second goes
		// by itself once one card holding a [gondor] token is left.
		assertTrue(scn.FreepsHasCardChoiceAvailable(garrison, heirs));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(wish));
		scn.FreepsChooseCard(garrison);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetCultureTokensOn(garrison));
		assertEquals(0, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(wish));

		// Any minion: an Orc, an Uruk-hai, and the exhausted Runner, whom the wound kills.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage, runner));
		// Not the companions, though they could take a wound: the bearer has 1 now, Pippin none.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, pippin));
		scn.FreepsChooseCard(runner);
		assertInDiscard(runner);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brego = scn.GetFreepsCard("brego");
		var knight = scn.GetFreepsCard("knight");
		var garrison = scn.GetFreepsCard("garrison");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight);
		scn.AttachCardsTo(knight, brego);
		scn.MoveCardsToSupportArea(garrison);
		scn.AddTokensToCard(garrison, 2);
		scn.AddWoundsToChar(knight, 2);

		scn.StartGame();
		assertAttachedTo(brego, knight);
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(2, scn.GetWoundsOn(knight));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Vitality 3 with 2 wounds: the Knight cannot exert.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(brego));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithOnlyOneGondorToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brego = scn.GetFreepsCard("brego");
		var knight = scn.GetFreepsCard("knight");
		var garrison = scn.GetFreepsCard("garrison");
		var wish = scn.GetFreepsCard("wish");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight);
		scn.AttachCardsTo(knight, brego);
		scn.MoveCardsToSupportArea(garrison, wish);
		scn.AddTokensToCard(garrison, 1);
		scn.AddTokensToCard(wish, 2);

		scn.StartGame();
		assertAttachedTo(brego, knight);
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertEquals(2, scn.GetCultureTokensOn(wish));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// One [gondor] token, beside two [dwarven] ones, cannot pay for two.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(brego));
		assertEquals(0, scn.GetWoundsOn(knight));
	}
}
