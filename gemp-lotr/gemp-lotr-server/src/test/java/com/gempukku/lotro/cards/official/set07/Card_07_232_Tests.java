package com.gempukku.lotro.cards.official.set07;

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

public class Card_07_232_Tests
{

	// The Fellowship path, so that Edoras Hall 7_330 (each [rohan] possession costs 1 less) is not under the play.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("firefoot", "7_232");
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion, strength 5, vitality 3; a skirmish ability used only on purpose
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ally; an archery ability that needs a villager
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit companion; a Man only beside 3 [rohan] companions or Eowyn
					put("rider2", "4_286");     // a second Rider of Rohan (not unique), with no mount: the [rohan] Man companion who is not the bearer
					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("troop4", "1_177");     // the minion that is already exhausted when the bearer's skirmish starts
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					// Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; its texts are about unbound Hobbits
					put("pursuer", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The regroup ability: Eomer, the conditions to discard, and a possession that is no condition.
	protected VirtualTableScenario GetRegroupScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("firefoot", "7_232");
					// Eomer, Skilled Tactician: [rohan] Man companion, vitality 3; a discount and a fellowship ability used
					// only on purpose
					put("eomer", "7_227");
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion; a skirmish ability used only on purpose
					// Well Stored: [rohan] condition in the support area; at the start of the fellowship phase it may heal a
					// villager, and there is none
					put("stored", "4_296");
					// Protecting the Hall: [rohan] condition borne by a [rohan] Man; an assignment ability used only on purpose
					put("hall", "11_149");
					put("chain", "4_278");      // Heavy Chain: [rohan] possession (armor) borne by a Man; a standing overwhelm text
					// Seeking Its Master: [sauron] condition in the Shadow player's support area; a Shadow ability that
					// needs a [sauron] Orc
					put("seeking", "1_276");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FirefootStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Firefoot, Éomer's Steed
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Mount
		 * Game Text: Bearer must be a [rohan] Man.
		 * 	At the start of each skirmish involving bearer, each minion skirmishing bearer must exert.
		 * 	<b>Regroup:</b> If bearer is Éomer, exert him to discard a condition. Any Shadow player may remove (1) to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("firefoot");

		assertEquals("Firefoot", card.getBlueprint().getTitle());
		assertEquals("Éomer's Steed", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedOnARohanManCompanionOrAllyAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var firefoot = scn.GetFreepsCard("firefoot");
		var rider = scn.GetFreepsCard("rider");
		var aldor = scn.GetFreepsCard("aldor");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(rider, boromir, merry);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToHand(firefoot);

		scn.StartGame();

		assertInHand(firefoot);
		assertTrue(scn.FreepsPlayAvailable(firefoot));
		scn.FreepsPlayCard(firefoot);
		// A [rohan] Man companion and a [rohan] Man ally; not a [gondor] Man, nor Merry, a [rohan] Hobbit who is no
		// Man beside two [rohan] companions and no Eowyn, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, aldor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, merry, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(rider);
		assertAttachedTo(firefoot, rider);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfBearersSkirmishEachMinionSkirmishingHimExertsAndNoOtherSkirmishDoesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firefoot = scn.GetFreepsCard("firefoot");
		var rider = scn.GetFreepsCard("rider");
		var rider2 = scn.GetFreepsCard("rider2");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var troop4 = scn.GetShadowCard("troop4");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(rider, rider2);
		scn.AttachCardsTo(rider, firefoot);

		scn.StartGame();
		assertAttachedTo(firefoot, rider);
		// The second Rider is given the strength to live through the Troop: 5 + 4 against 13, which is under double.
		scn.ApplyAdHocModifier(new StrengthModifier(null, rider2, null, 4));
		scn.MoveMinionsToTable(troop1, savage, troop2, troop3, troop4);
		// Vitality 3 with 2 wounds: exhausted, so a wound would kill it where an exertion cannot be paid.
		scn.AddWoundsToChar(troop4, 2);
		assertEquals(2, scn.GetWoundsOn(troop4));
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{rider, troop1}, new PhysicalCardImpl[]{rider2, troop2}, new PhysicalCardImpl[]{frodo, troop3});
		// The Uruk Savage and the exhausted Troop were left over: the Shadow player puts them on the Rider as well.
		scn.ShadowAssignToMinions(rider, savage, troop4);

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

		// The bearer's skirmish: an Orc and an Uruk-hai, each exerts once; the exhausted Troop beside them is left as it
		// was; the Troop that fought the second Rider does not exert, nor the one assigned to Frodo, whose skirmish has
		// not begun.
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, troop4);
		assertEquals(2, scn.GetWoundsOn(troop4));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(troop3));
	}

	@Test
	public void AMinionSkirmishingBearerAgainInTheFierceRoundExertsAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firefoot = scn.GetFreepsCard("firefoot");
		var rider = scn.GetFreepsCard("rider");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, firefoot);

		scn.StartGame();
		assertAttachedTo(firefoot, rider);
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
	public void RegroupAbilityExertsEomerToDiscardAConditionOfEitherSideWhenTheShadowPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var firefoot = scn.GetFreepsCard("firefoot");
		var eomer = scn.GetFreepsCard("eomer");
		var rider = scn.GetFreepsCard("rider");
		var stored = scn.GetFreepsCard("stored");
		var hall = scn.GetFreepsCard("hall");
		var chain = scn.GetFreepsCard("chain");
		var seeking = scn.GetShadowCard("seeking");
		scn.MoveCompanionsToTable(eomer, rider);
		scn.AttachCardsTo(eomer, firefoot);
		scn.AttachCardsTo(rider, hall, chain);
		scn.MoveCardsToSupportArea(stored, seeking);

		scn.StartGame();
		assertAttachedTo(firefoot, eomer);
		assertAttachedTo(hall, rider);
		assertAttachedTo(chain, rider);
		assertInZone(Zone.SUPPORT, stored, seeking);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(1);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(firefoot));
		scn.FreepsUseCardAction(firefoot);

		// The cost: Eomer exerts.  The Shadow player, with (1) in the pool, is asked whether to prevent it.
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();

		// A condition of either side, in a support area or borne; not a possession.
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(stored, hall, seeking));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(chain, firefoot));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(seeking);

		assertInDiscard(seeking);
		assertInZone(Zone.SUPPORT, stored);
		assertAttachedTo(hall, rider);
		assertAttachedTo(chain, rider);
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayRemoveOneTwilightToPreventTheDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var firefoot = scn.GetFreepsCard("firefoot");
		var eomer = scn.GetFreepsCard("eomer");
		var stored = scn.GetFreepsCard("stored");
		scn.MoveCompanionsToTable(eomer);
		scn.AttachCardsTo(eomer, firefoot);
		scn.MoveCardsToSupportArea(stored);

		scn.StartGame();
		assertAttachedTo(firefoot, eomer);
		assertInZone(Zone.SUPPORT, stored);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(1);

		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(eomer));
		scn.FreepsUseCardAction(firefoot);
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseYes();

		// The (1) is removed and nothing is discarded; Eomer's exertion stays paid.
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, stored);
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithTheTwilightPoolEmptyTheShadowPlayerIsNotAskedAndTheConditionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var firefoot = scn.GetFreepsCard("firefoot");
		var eomer = scn.GetFreepsCard("eomer");
		var stored = scn.GetFreepsCard("stored");
		var seeking = scn.GetShadowCard("seeking");
		scn.MoveCompanionsToTable(eomer);
		scn.AttachCardsTo(eomer, firefoot);
		scn.MoveCardsToSupportArea(stored, seeking);

		scn.StartGame();
		assertAttachedTo(firefoot, eomer);
		assertInZone(Zone.SUPPORT, stored, seeking);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(0);

		assertEquals(0, scn.GetTwilight());
		scn.FreepsUseCardAction(firefoot);

		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(stored, seeking));
		scn.FreepsChooseCard(stored);
		assertInDiscard(stored);
		assertInZone(Zone.SUPPORT, seeking);
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoConditionInPlayEomerExertsForNothingAndNoPreventionIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var firefoot = scn.GetFreepsCard("firefoot");
		var eomer = scn.GetFreepsCard("eomer");
		var rider = scn.GetFreepsCard("rider");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(eomer, rider);
		scn.AttachCardsTo(eomer, firefoot);
		scn.AttachCardsTo(rider, chain);

		scn.StartGame();
		assertAttachedTo(firefoot, eomer);
		assertAttachedTo(chain, rider);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(1);

		assertEquals(0, scn.GetWoundsOn(eomer));
		assertTrue(scn.FreepsActionAvailable(firefoot));
		scn.FreepsUseCardAction(firefoot);

		// Nothing to discard: the prevention is not put (Q-0114), and the pool keeps its (1).
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertEquals(1, scn.GetTwilight());
		assertAttachedTo(chain, rider);
		assertAttachedTo(firefoot, eomer);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhenBearerIsNotEomerThoughEomerIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var firefoot = scn.GetFreepsCard("firefoot");
		var eomer = scn.GetFreepsCard("eomer");
		var rider = scn.GetFreepsCard("rider");
		var stored = scn.GetFreepsCard("stored");
		scn.MoveCompanionsToTable(eomer, rider);
		scn.AttachCardsTo(rider, firefoot);
		scn.MoveCardsToSupportArea(stored);

		scn.StartGame();
		assertAttachedTo(firefoot, rider);
		assertInZone(Zone.SUPPORT, stored);
		scn.SkipToPhase(Phase.REGROUP);

		// The Rider, a [rohan] Man who can exert, bears it; Eomer is in play beside him.
		assertEquals(0, scn.GetWoundsOn(rider));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(firefoot));
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileEomerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var firefoot = scn.GetFreepsCard("firefoot");
		var eomer = scn.GetFreepsCard("eomer");
		var stored = scn.GetFreepsCard("stored");
		scn.MoveCompanionsToTable(eomer);
		scn.AttachCardsTo(eomer, firefoot);
		scn.MoveCardsToSupportArea(stored);

		scn.StartGame();
		assertAttachedTo(firefoot, eomer);
		assertInZone(Zone.SUPPORT, stored);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(eomer, 2);
		assertEquals(2, scn.GetWoundsOn(eomer));
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetWoundsOn(eomer));
		assertFalse(scn.FreepsActionAvailable(firefoot));
		assertInZone(Zone.SUPPORT, stored);
	}
}
