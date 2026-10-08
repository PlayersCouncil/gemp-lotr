package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.WoundCharactersEffect;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_097_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fell", "54_97");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: strength 7, vitality 4; a fellowship ability used only on purpose
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf, strength 6, vitality 4
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, strength 13, no text
					put("troop2", "1_177");     // a second Goblin Patrol Troop
					put("slayer", "3_93");      // Morgul Slayer: [sauron] Orc minion, strength 7; a regroup ability used only on purpose
					put("toldea", "7_219");     // Ulaire Toldea, Wraith on Wings: fierce Nazgul, strength 12; a twilight discount when cheated in does nothing
					put("denizen", "13_119");   // Underdeeps Denizen: [orc] Orc minion that cannot take wounds while at an underground site
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LongIFellStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: 4
		 * Name: Long I Fell
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spell.
		 * Skirmish: Spot Gandalf to make him unable to take wounds. Any Shadow player may make you wound a minion
		 * to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fell");

		assertEquals("Long I Fell", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithoutGandalfThoughAGandalfCompanionIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetFreepsCard("fell");
		var avenger = scn.GetFreepsCard("avenger");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(fell);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(avenger, troop);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fell));
		assertInHand(fell);
	}

	@Test
	public void GandalfTakesNoWoundsInTheSkirmishItIsPlayedInAndWoundsAgainInTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetFreepsCard("fell");
		var gandalf = scn.GetFreepsCard("gandalf");
		var toldea = scn.GetShadowCard("toldea");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(fell);

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gandalf, toldea);

		assertTrue(scn.FreepsPlayAvailable(fell));
		int twilight = scn.GetTwilight();
		scn.FreepsPlayCard(fell);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();
		assertInDiscard(fell);
		assertEquals(0, scn.GetWoundsOn(toldea));

		// Toldea (12) beats Gandalf (7) without overwhelming him: no wound.
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(gandalf));

		// Each skirmish is a skirmish phase of its own (CR 4.2 "skirmish phase"): in the fierce skirmish he is wounded.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(gandalf, toldea);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyGandalfIsProtectedAndOnlyInTheSkirmishPhaseItIsPlayedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetFreepsCard("fell");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var troop = scn.GetShadowCard("troop");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(fell);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, troop}, new PhysicalCardImpl[]{avenger, slayer});
		// The event is played in the Ent Avenger's skirmish, which is resolved first; Gandalf's comes after it.
		scn.FreepsResolveSkirmish(avenger);

		scn.FreepsPlayCard(fell);
		scn.ShadowChooseNo();
		assertInDiscard(fell);

		// Morgul Slayer (7) beats the Ent Avenger (6), a [gandalf] companion who is not Gandalf.
		assertEquals(0, scn.GetWoundsOn(avenger));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(avenger));

		// Goblin Patrol Troop (13) beats Gandalf (7) in the next skirmish phase, and he is wounded.
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void GandalfIsProtectedWhileHeStandsOutsideTheSkirmishItIsPlayedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetFreepsCard("fell");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var troop = scn.GetShadowCard("troop");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(fell);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, troop}, new PhysicalCardImpl[]{avenger, slayer});
		// The Ent Avenger's skirmish is the one being fought; Gandalf's has not begun.
		scn.FreepsResolveSkirmish(avenger);
		assertFalse(scn.IsCharSkirmishing(gandalf));

		// Before the event, a wound from outside the skirmish reaches him.
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsExecuteAdHocEffect(new WoundCharactersEffect(slayer, gandalf)); // lint-ok: no card under test wounds a companion who is not in the skirmish being fought
		assertEquals(1, scn.GetWoundsOn(gandalf));
		scn.ShadowPass();

		scn.FreepsPlayCard(fell);
		scn.ShadowChooseNo();
		assertInDiscard(fell);

		// The same wound again: "him" is Gandalf wherever he stands, and it comes to nothing.
		assertFalse(scn.IsCharSkirmishing(gandalf));
		scn.ShadowExecuteAdHocEffectUnchecked(new WoundCharactersEffect(slayer, gandalf)); // lint-ok: the same wound, which the event is to stop
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ShadowPlayerMayMakeYouWoundAMinionOfYourChoosingToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetFreepsCard("fell");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var slayer = scn.GetShadowCard("slayer");
		var denizen = scn.GetShadowCard("denizen");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(fell);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop, troop2, slayer, denizen);
		// The fellowship moves to site 4, Balin's Tomb, which is underground: the Underdeeps Denizen cannot take wounds there.
		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, troop}, new PhysicalCardImpl[]{avenger, slayer});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);

		int twilight = scn.GetTwilight();
		scn.FreepsPlayCard(fell);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseYes();

		// The Free Peoples player chooses the minion: the one skirmishing Gandalf, one in another skirmish, or one
		// assigned to no skirmish at all.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, slayer, troop2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(denizen, gandalf, avenger));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(troop2));
		scn.FreepsChooseCard(troop2);
		assertEquals(1, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInDiscard(fell);

		// Prevented: Gandalf loses to the Goblin Patrol Troop and takes his wound.
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void ShadowPlayerIsNotAskedWhenNoMinionCanTakeAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetFreepsCard("fell");
		var gandalf = scn.GetFreepsCard("gandalf");
		var denizen = scn.GetShadowCard("denizen");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(fell);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(denizen);
		// Strength 6 + 6 = 12: it beats Gandalf (7) without overwhelming him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, denizen, null, 6));
		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignAndResolve(gandalf, denizen);

		scn.FreepsPlayCard(fell);
		assertInDiscard(fell);
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
