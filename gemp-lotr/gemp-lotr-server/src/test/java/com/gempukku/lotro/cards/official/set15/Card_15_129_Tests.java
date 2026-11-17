package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_129_Tests
{

	// The Fellowship path, so that Edoras Hall 7_330 (each [rohan] possession costs 1 less) is not under the play.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horse", "15_129");
					put("horse2", "15_129");
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion, strength 5, vitality 3; a skirmish ability used only on purpose
					put("rider2", "4_286");     // a second Rider of Rohan (not unique): the [rohan] Man companion who is not the bearer
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ally, strength 3, vitality 2; an archery ability that needs a villager
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit companion; a Man only beside 3 [rohan] companions or Eowyn
					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1 (exhausted as he comes)
					put("runner2", "1_178");
					// Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; its texts are about unbound Hobbits
					put("pursuer", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HorseofGreatStatureStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Horse of Great Stature
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Game Text: Bearer must be a [rohan] Man.
		 * 	At the start of each skirmish involving bearer, each minion skirmishing bearer must exert.
		 * 	<b>Skirmish:</b> Exert bearer to make a [rohan] Man strength +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horse");

		assertEquals("Horse of Great Stature", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedOnARohanManCompanionOrAllyAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var horse = scn.GetFreepsCard("horse");
		var rider = scn.GetFreepsCard("rider");
		var aldor = scn.GetFreepsCard("aldor");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(rider, boromir, merry);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToHand(horse);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(horse));
		scn.FreepsPlayCard(horse);
		// A [rohan] Man companion and a [rohan] Man ally; not a [gondor] Man, nor Merry, a [rohan] Hobbit who is no
		// Man beside two [rohan] companions and no Eowyn, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, aldor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, merry, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(rider);
		assertAttachedTo(horse, rider);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfBearersSkirmishEachMinionSkirmishingHimExertsAndNoOtherSkirmishDoesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horse = scn.GetFreepsCard("horse");
		var rider = scn.GetFreepsCard("rider");
		var rider2 = scn.GetFreepsCard("rider2");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var savage = scn.GetShadowCard("savage");
		var runner1 = scn.GetShadowCard("runner1");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(rider, rider2);
		scn.AttachCardsTo(rider, horse);

		scn.StartGame();
		assertAttachedTo(horse, rider);
		// The second Rider is given the strength to live through the Troop: 5 + 4 against 13, which is under double.
		scn.ApplyAdHocModifier(new StrengthModifier(null, rider2, null, 4));
		scn.MoveMinionsToTable(troop1, savage, runner1, troop2, troop3);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{rider, troop1}, new PhysicalCardImpl[]{rider2, troop2}, new PhysicalCardImpl[]{frodo, troop3});
		// The Uruk Savage and the Goblin Runner were left over: the Shadow player puts them on the Rider as well.
		scn.ShadowAssignToMinions(rider, savage, runner1);

		// The second Rider's skirmish first, a [rohan] Man with no mount: nobody exerts, those assigned to the bearer included.
		scn.FreepsResolveSkirmish(rider2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(troop3));

		scn.PassSkirmishActions();
		// The second Rider, 9 against 13, loses and takes a wound.
		assertEquals(1, scn.GetWoundsOn(rider2));
		assertEquals(0, scn.GetWoundsOn(troop2));

		// The bearer's skirmish: an Orc and an Uruk-hai, each exerts once; the Goblin Runner, exhausted at vitality 1,
		// cannot exert and takes no wound (a wound would kill him). The Troop that fought the second Rider does not
		// exert, nor the one assigned to Frodo, whose skirmish has not begun.
		assertEquals(0, scn.GetWoundsOn(runner1));
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner1));
		assertInPlay(runner1);
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(troop3));
	}

	@Test
	public void AMinionSkirmishingBearerAgainInTheFierceRoundExertsAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horse = scn.GetFreepsCard("horse");
		var rider = scn.GetFreepsCard("rider");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, horse);

		scn.StartGame();
		assertAttachedTo(horse, rider);
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rider, pursuer);

		assertEquals(0, scn.GetWoundsOn(pursuer));
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(pursuer));

		// 5 against 5: a tie goes to the Shadow side, so the Rider takes a wound and the Pursuer none.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(pursuer));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(rider, pursuer);
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetWoundsOn(pursuer));
	}

	@Test
	public void SkirmishAbilityExertsItsOwnBearerToMakeARohanManAnywhereStrengthPlus1ForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var horse = scn.GetFreepsCard("horse");
		var horse2 = scn.GetFreepsCard("horse2");
		var rider = scn.GetFreepsCard("rider");
		var aldor = scn.GetFreepsCard("aldor");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(rider, boromir, merry);
		scn.MoveAlliesToTable(aldor);
		scn.AttachCardsTo(rider, horse);
		scn.AttachCardsTo(aldor, horse2);

		scn.StartGame();
		assertAttachedTo(horse, rider);
		assertAttachedTo(horse2, aldor);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{rider, runner1}, new PhysicalCardImpl[]{boromir, runner2});
		scn.FreepsResolveSkirmish(rider);

		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(aldor));
		assertEquals(5, scn.GetStrength(rider));
		assertEquals(3, scn.GetStrength(aldor));
		assertTrue(scn.FreepsActionAvailable(horse));
		scn.FreepsUseCardAction(horse);
		// The Rider, its bearer, exerts; Aldor, who bears the other copy, does not.
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(aldor));
		// Merry's own offer, to exert him and heal a [rohan] Man who exerts, is declined.
		scn.FreepsDeclineOptionalTrigger();
		// The Rider in this skirmish and Aldor in none; not Boromir, a Man of [gondor], nor Merry, a [rohan] Hobbit who
		// is no Man beside one other [rohan] companion, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, aldor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, merry, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aldor);
		assertEquals(4, scn.GetStrength(aldor));
		assertEquals(5, scn.GetStrength(rider));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		// The other copy, borne by an ally, exerts Aldor and not the Rider.
		scn.FreepsUseCardAction(horse2);
		assertEquals(1, scn.GetWoundsOn(aldor));
		assertEquals(1, scn.GetWoundsOn(rider));
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsChooseCard(rider);
		assertEquals(6, scn.GetStrength(rider));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 6 against 5: the Rider wins. In Boromir's skirmish, the next, Aldor's bonus is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(aldor));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileItsBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horse = scn.GetFreepsCard("horse");
		var horse2 = scn.GetFreepsCard("horse2");
		var rider = scn.GetFreepsCard("rider");
		var aldor = scn.GetFreepsCard("aldor");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(rider);
		scn.MoveAlliesToTable(aldor);
		scn.AttachCardsTo(rider, horse);
		scn.AttachCardsTo(aldor, horse2);
		scn.AddWoundsToChar(rider, 2);

		scn.StartGame();
		assertAttachedTo(horse, rider);
		assertAttachedTo(horse2, aldor);
		assertEquals(2, scn.GetWoundsOn(rider));
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rider, runner1);
		scn.FreepsResolveSkirmish(rider);

		// Vitality 3 with 2 wounds: the Rider cannot exert, so his mount's ability is not offered; the other copy's is.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(horse));
		assertTrue(scn.FreepsActionAvailable(horse2));
	}
}
