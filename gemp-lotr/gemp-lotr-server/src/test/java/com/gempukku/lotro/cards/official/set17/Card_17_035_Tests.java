package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_17_035_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cache1", "17_35");
					put("cache2", "17_35");

					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, vitality 3, no ranger; Knight is its whole text
					// Gondorian Merchant: the [gondor] Man ally, vitality 1; his maneuver ability is used only on purpose
					put("merchant", "7_97");
					// Narsil, Blade of the Faithful: vitality +1 on a [gondor] Man, so that the Merchant can exert; its
					// response answers a wound to its bearer, who takes none here
					put("narsil", "9_34");
					// Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit); a skirmish ability used only
					// on purpose
					put("pippin", "103_31");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; Valiant is its whole text
					// Arwen, Daughter of Elrond: an [elven] Elf ranger, vitality 3; strength against a Nazgul only
					put("arwen", "1_30");
					// Aragorn, Ranger of the North: a [gondor] Man ranger, vitality 4; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Ranger of Osgiliath: a [gondor] Man ranger, vitality 3; a regroup ability used only on purpose
					put("osgiliath", "7_116");

					// Garrison of Osgiliath: [gondor] support-area condition that takes 2 [gondor] tokens as it is
					// played; its skirmish ability is not used
					put("garrison", "6_52");
					// Heirs of Gondor: [gondor] support-area condition that takes a [gondor] token as it is played; its
					// skirmish ability is not used
					put("heirs", "13_69");
					// Ithilien Trap: [gondor] support-area condition that takes [gondor] tokens as [gondor] Men win
					// skirmishes, here with none; its maneuver ability is not used
					put("trap", "4_126");
					// Stout and Strong: [dwarven] support-area condition that takes [dwarven] tokens as Dwarves win
					// skirmishes; its skirmish ability is not used
					put("stout", "4_57");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1
					// Goblin Runner: [moria] Orc, vitality 1; its "when you play" is not reached, cheated into play
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SoldiersCacheStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Soldier's Cache
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Site Number: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Fortification</b>.<br>At the start of your maneuver phase, you may spot 3 rangers or exert a [gondor] Man to transfer this condition to a minion. Wound that minion.<br><b>Regroup:</b> Exert a ranger to reinforce a [gondor] token.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cache1");

		assertEquals("Soldier's Cache", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithOnlyTheExertionOpenAGondorManExertsToTransferItToAMinionWhoIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var cache1 = scn.GetFreepsCard("cache1");
		var cache2 = scn.GetFreepsCard("cache2");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var arwen = scn.GetFreepsCard("arwen");
		// One ranger (Arwen): the exertion is the one price open.
		scn.MoveCompanionsToTable(knight, pippin, guard, arwen);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToSupportArea(cache1, cache2);
		assertInZone(Zone.SUPPORT, cache1, cache2);
		assertAttachedTo(narsil, merchant);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));

		// Each copy makes its own offer as the maneuver phase starts.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(cache1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(cache2));
		scn.FreepsChooseOptionalTrigger(cache1);

		// [gondor] Men, a companion and the ally; not a [gondor] Hobbit, a [rohan] Man, the Elf ranger or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, arwen, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(knight);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(guard));

		// Minions of two cultures and races; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		// The copy used goes to the Savage, who is wounded; the Troop is not. The other copy stays in the support area.
		assertAttachedTo(cache1, savage);
		assertInZone(Zone.SUPPORT, cache2);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));

		// The second copy's offer is still open; declined, it stays where it is.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(cache2));
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, cache2);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoGondorManAbleToExertThreeRangersAreSpottedAndNobodyIsAskedOrExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cache1 = scn.GetFreepsCard("cache1");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		scn.MoveCompanionsToTable(arwen, aragorn, osgiliath);
		scn.MoveCardsToSupportArea(cache1);
		assertInZone(Zone.SUPPORT, cache1);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// Rangers of two cultures; both [gondor] Men exhausted (vitality 4 and 3).
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(osgiliath, 2);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(osgiliath));
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(cache1));
		scn.FreepsAcceptOptionalTrigger();

		// No question of which price: the minion is asked for at once.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		scn.FreepsChooseCard(troop);

		assertAttachedTo(cache1, troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		// Nobody exerts.
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(osgiliath));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithBothPricesOpenThePlayerIsAskedAndSpottingThreeRangersExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cache1 = scn.GetFreepsCard("cache1");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		scn.MoveCompanionsToTable(arwen, aragorn, osgiliath);
		scn.MoveCardsToSupportArea(cache1);
		assertInZone(Zone.SUPPORT, cache1);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(osgiliath));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsAcceptOptionalTrigger();

		// Either price can be paid, so the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Spot 3 rangers"));
		assertTrue(scn.FreepsChoiceAvailable("Exert a"));
		scn.FreepsChooseOption("Spot 3 rangers");

		// The one minion is chosen unasked.
		assertAttachedTo(cache1, troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(osgiliath));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithTwoRangersAndNoGondorManAbleToExertItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cache1 = scn.GetFreepsCard("cache1");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(arwen, aragorn, knight, pippin, guard);
		scn.MoveCardsToSupportArea(cache1);
		assertInZone(Zone.SUPPORT, cache1);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Two rangers, and the Knight beside them no ranger; the two [gondor] Men exhausted. A [gondor] Hobbit and
		// a [rohan] Man could exert, but do not match.
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(knight, 2);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(knight));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(cache1));
		assertInZone(Zone.SUPPORT, cache1);
		assertEquals(0, scn.GetWoundsOn(troop));
	}

	@Test
	public void DeclinedItStaysInTheSupportAreaAndIsNotOfferedAtTheStartOfTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cache1 = scn.GetFreepsCard("cache1");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(cache1);
		assertInZone(Zone.SUPPORT, cache1);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(cache1));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, cache1);
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The start of the next phase sets nothing off.
		scn.PassCurrentPhaseActions();
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(cache1));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertInZone(Zone.SUPPORT, cache1);
	}

	@Test
	public void DeclinedItIsOfferedAgainAtTheNextManeuverPhaseOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cache1 = scn.GetFreepsCard("cache1");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(cache1);
		assertInZone(Zone.SUPPORT, cache1);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(cache1));
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, cache1);

		// A second move: a second maneuver phase in the same turn, with the Troop still in play.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowRefreshDecision();
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(cache1));
		scn.FreepsAcceptOptionalTrigger();

		// The Knight, the one [gondor] Man, and the Troop, the one minion, are chosen unasked.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertAttachedTo(cache1, troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void BorneByAMinionItIsOfferedAgainAtTheNextManeuverPhaseAndMovesToAnotherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cache1 = scn.GetFreepsCard("cache1");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(cache1);
		assertInZone(Zone.SUPPORT, cache1);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(troop);
		assertAttachedTo(cache1, troop);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(troop));

		// A second maneuver phase in the same turn, both minions still in play.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowRefreshDecision();
		scn.SkipToPhase(Phase.MANEUVER);

		// The text says only "transfer this condition to a minion": it is offered from the Troop as well
		// (Ketura's ruling, Q-0369: A, "there's a good reason to want to re-transfer (you get another wound)"),
		// and the Troop, its bearer, is no choice; the Savage, the one minion left, is chosen unasked.
		assertAttachedTo(cache1, troop);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(cache1));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(knight));
		assertAttachedTo(cache1, savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheWoundKillsAnExhaustedMinionAndTheCacheGoesWithIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cache1 = scn.GetFreepsCard("cache1");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(cache1);
		assertInZone(Zone.SUPPORT, cache1);

		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.IsExhausted(runner));
		scn.FreepsAcceptOptionalTrigger();
		// An exhausted minion is a minion: it is offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, runner));
		scn.FreepsChooseCard(runner);

		// Vitality 1: the wound kills it, and the condition it bore is discarded with it.
		assertInDiscard(runner, cache1);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsARangerToReinforceAGondorToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var cache1 = scn.GetFreepsCard("cache1");
		var arwen = scn.GetFreepsCard("arwen");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		var knight = scn.GetFreepsCard("knight");
		var garrison = scn.GetFreepsCard("garrison");
		var heirs = scn.GetFreepsCard("heirs");
		var trap = scn.GetFreepsCard("trap");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCompanionsToTable(arwen, osgiliath, knight);
		scn.MoveCardsToSupportArea(cache1, garrison, heirs, trap, stout);
		// The tokens each card takes in play: 2 as the Garrison is played, 1 as Heirs is, a [dwarven] one for a
		// Dwarf's win; none on the Trap.
		scn.AddTokensToCard(garrison, 2);
		scn.AddTokensToCard(heirs, 1);
		scn.AddTokensToCard(stout, 1);
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(0, scn.GetCultureTokensOn(trap));

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(osgiliath));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(cache1));
		scn.FreepsUseCardAction(cache1);

		// Rangers of two cultures; not a [gondor] Man who is no ranger, or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, osgiliath));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(osgiliath));
		assertEquals(0, scn.GetWoundsOn(knight));

		// The cards with a [gondor] token; not the [gondor] Trap with none or the [dwarven] card with its own.
		assertTrue(scn.FreepsHasCardChoiceAvailable(garrison, heirs));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(trap, stout, cache1));
		scn.FreepsChooseCard(heirs);
		assertEquals(2, scn.GetCultureTokensOn(heirs));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(0, scn.GetCultureTokensOn(trap));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileTheOnlyRangerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cache1 = scn.GetFreepsCard("cache1");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(arwen, knight);
		scn.MoveCardsToSupportArea(cache1, garrison);
		scn.AddTokensToCard(garrison, 2);
		assertEquals(2, scn.GetCultureTokensOn(garrison));

		scn.StartGame();
		// Vitality 3 with 2 wounds: exhausted. The Knight could exert, but is no ranger.
		scn.AddWoundsToChar(arwen, 2);
		assertEquals(2, scn.GetWoundsOn(arwen));
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(cache1));

		// One wound off her, and it is offered.
		scn.RemoveWoundsFromChar(arwen, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(cache1));
	}

	@Test
	public void WithNoGondorTokenInPlayTheRegroupAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cache1 = scn.GetFreepsCard("cache1");
		var arwen = scn.GetFreepsCard("arwen");
		var trap = scn.GetFreepsCard("trap");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(cache1, trap, stout);
		scn.AddTokensToCard(stout, 1);
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(0, scn.GetCultureTokensOn(trap));

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsActionAvailable(cache1));
		scn.FreepsUseCardAction(cache1);

		// The one ranger exerts unasked, and there is no [gondor] token to reinforce.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(0, scn.GetCultureTokensOn(trap));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
