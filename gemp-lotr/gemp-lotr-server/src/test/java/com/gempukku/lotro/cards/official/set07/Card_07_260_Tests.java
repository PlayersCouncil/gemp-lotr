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

public class Card_07_260_Tests
{

	// The Fellowship path, so that Edoras Hall 7_330 (each [rohan] possession costs 1 less) is not under the play.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("windfola", "7_260");
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

	// The skirmish ability: Eowyn, fierce minions of two cultures and races, and one that is not fierce.
	protected VirtualTableScenario GetSkirmishAbilityScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("windfola", "7_260");
					// Eowyn, Restless Maiden: [rohan] Man companion, strength 6, vitality 3; her skirmish ability (discard a
					// possession she bears) is used only on purpose
					put("eowyn", "7_229");
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion, strength 5, vitality 3; a skirmish ability used only on purpose
					// Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; its texts are about unbound Hobbits
					put("pursuer", "4_190");
					// Black Rider: [wraith] Nazgul, fierce, strength 8 (+1 for each companion), vitality 3
					put("blackrider", "12_161");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, not fierce, strength 13, vitality 3, no text
					// White Hand Destroyer: [uruk-hai] Uruk-hai, fierce, strength 10, vitality 3; hunter only for sites controlled
					put("destroyer", "17_124");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WindfolaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Windfola
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Game Text: Bearer must be a [rohan] Man.
		 * 	At the start of each skirmish involving bearer, each minion skirmishing bearer must exert.
		 * 	<b>Skirmish:</b> If bearer is Éowyn, exert her twice to wound a fierce minion she is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("windfola");

		assertEquals("Windfola", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
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
		var windfola = scn.GetFreepsCard("windfola");
		var rider = scn.GetFreepsCard("rider");
		var aldor = scn.GetFreepsCard("aldor");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(rider, boromir, merry);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToHand(windfola);

		scn.StartGame();

		assertInHand(windfola);
		assertTrue(scn.FreepsPlayAvailable(windfola));
		scn.FreepsPlayCard(windfola);
		// A [rohan] Man companion and a [rohan] Man ally; not a [gondor] Man, nor Merry, a [rohan] Hobbit who is no
		// Man beside two [rohan] companions and no Eowyn, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, aldor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, merry, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(rider);
		assertAttachedTo(windfola, rider);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfBearersSkirmishEachMinionSkirmishingHimExertsAndNoOtherSkirmishDoesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var windfola = scn.GetFreepsCard("windfola");
		var rider = scn.GetFreepsCard("rider");
		var rider2 = scn.GetFreepsCard("rider2");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var troop4 = scn.GetShadowCard("troop4");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(rider, rider2);
		scn.AttachCardsTo(rider, windfola);

		scn.StartGame();
		assertAttachedTo(windfola, rider);
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

		var windfola = scn.GetFreepsCard("windfola");
		var rider = scn.GetFreepsCard("rider");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, windfola);

		scn.StartGame();
		assertAttachedTo(windfola, rider);
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
	public void SkirmishAbilityExertsEowynTwiceToWoundAFierceMinionSheIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishAbilityScenario();

		var windfola = scn.GetFreepsCard("windfola");
		var eowyn = scn.GetFreepsCard("eowyn");
		var rider = scn.GetFreepsCard("rider");
		var pursuer = scn.GetShadowCard("pursuer");
		var blackrider = scn.GetShadowCard("blackrider");
		var troop = scn.GetShadowCard("troop");
		var destroyer = scn.GetShadowCard("destroyer");
		scn.MoveCompanionsToTable(eowyn, rider);
		scn.AttachCardsTo(eowyn, windfola);

		scn.StartGame();
		assertAttachedTo(windfola, eowyn);
		scn.MoveMinionsToTable(pursuer, blackrider, troop, destroyer);
		// The Black Rider, vitality 3, starts with a wound: the mount's exertion leaves him exhausted, so a wound kills
		// him where an exertion could not be placed.
		scn.AddWoundsToChar(blackrider, 1);
		assertEquals(1, scn.GetWoundsOn(blackrider));
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{eowyn, pursuer}, new PhysicalCardImpl[]{rider, destroyer});
		// The Black Rider and the Troop were left over: the Shadow player puts them on Eowyn as well.
		scn.ShadowAssignToMinions(eowyn, blackrider, troop);

		// Eowyn's skirmish first: the mount's trigger exerts each of her three minions once.
		scn.FreepsResolveSkirmish(eowyn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(2, scn.GetWoundsOn(blackrider));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(destroyer));
		assertEquals(0, scn.GetWoundsOn(eowyn));

		assertTrue(scn.FreepsActionAvailable(windfola));
		scn.FreepsUseCardAction(windfola);

		// The cost: Eowyn exerts twice.  Then a fierce minion she is skirmishing, of either culture and race; not the
		// Troop, who is not fierce, nor the Destroyer, fierce but assigned to the Rider, whose skirmish has not begun.
		// The exhausted Black Rider is offered: a wound needs no vitality to spare.
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(pursuer, blackrider));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop, destroyer));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(blackrider);

		// His third wound kills him.
		assertInDiscard(blackrider);
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(destroyer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UsedInAnotherCompanionsSkirmishEowynExertsTwiceAndNoMinionIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishAbilityScenario();

		var windfola = scn.GetFreepsCard("windfola");
		var eowyn = scn.GetFreepsCard("eowyn");
		var rider = scn.GetFreepsCard("rider");
		var pursuer = scn.GetShadowCard("pursuer");
		var destroyer = scn.GetShadowCard("destroyer");
		scn.MoveCompanionsToTable(eowyn, rider);
		scn.AttachCardsTo(eowyn, windfola);

		scn.StartGame();
		assertAttachedTo(windfola, eowyn);
		scn.MoveMinionsToTable(pursuer, destroyer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{eowyn, pursuer}, new PhysicalCardImpl[]{rider, destroyer});

		// The Rider's skirmish first, against the fierce Destroyer; Eowyn waits for hers against the fierce Pursuer.
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(destroyer));
		assertEquals(0, scn.GetWoundsOn(pursuer));

		// The text names no skirmish of hers, so the ability is offered; but no fierce minion is skirmishing her.
		assertTrue(scn.FreepsActionAvailable(windfola));
		scn.FreepsUseCardAction(windfola);

		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(destroyer));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhenBearerIsNotEowynThoughEowynIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishAbilityScenario();

		var windfola = scn.GetFreepsCard("windfola");
		var eowyn = scn.GetFreepsCard("eowyn");
		var rider = scn.GetFreepsCard("rider");
		var pursuer = scn.GetShadowCard("pursuer");
		var blackrider = scn.GetShadowCard("blackrider");
		scn.MoveCompanionsToTable(eowyn, rider);
		scn.AttachCardsTo(rider, windfola);

		scn.StartGame();
		assertAttachedTo(windfola, rider);
		scn.MoveMinionsToTable(pursuer, blackrider);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{rider, pursuer}, new PhysicalCardImpl[]{eowyn, blackrider});

		// The Rider, a [rohan] Man who could exert twice, bears it in his skirmish against a fierce minion.
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(rider));
		assertFalse(scn.FreepsActionAvailable(windfola));

		// 5 against 5: a tie goes to the Shadow side, and the Rider takes a wound.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(rider));

		// Eowyn's own skirmish against a fierce minion: she does not bear it, and it is not offered there either.
		scn.FreepsResolveSkirmish(eowyn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertFalse(scn.FreepsActionAvailable(windfola));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileEowynCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishAbilityScenario();

		var windfola = scn.GetFreepsCard("windfola");
		var eowyn = scn.GetFreepsCard("eowyn");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(eowyn);
		scn.AttachCardsTo(eowyn, windfola);

		scn.StartGame();
		assertAttachedTo(windfola, eowyn);
		// Vitality 3 with 1 wound: 2 left, and exerting twice needs 3.
		scn.AddWoundsToChar(eowyn, 1);
		assertEquals(1, scn.GetWoundsOn(eowyn));
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, pursuer);

		scn.FreepsResolveSkirmish(eowyn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertFalse(scn.FreepsActionAvailable(windfola));
	}
}
