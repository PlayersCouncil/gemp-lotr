package com.gempukku.lotro.cards.official.set04;

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

public class Card_04_097_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fell", "4_97");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: strength 7, vitality 4; a fellowship ability used only on purpose
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf, strength 6, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, strength 13, no text
					put("troop2", "1_177");     // a second Goblin Patrol Troop
					put("slayer", "3_93");      // Morgul Slayer: [sauron] Orc minion, strength 7; a regroup ability used only on purpose
					put("toldea", "7_219");     // Ulaire Toldea, Wraith on Wings: fierce Nazgul, strength 12; a twilight discount when cheated in does nothing
					put("denizen", "13_119");   // Underdeeps Denizen: [orc] Orc minion that cannot take wounds while at an underground site
					put("skirmisher", "18_90"); // Orkish Skirmisher: [orc] Orc, strength 9, vitality 3; each time he wins a skirmish he may exert twice to wound a companion
					put("skirmisher2", "18_90");
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
		 * Game Text: <b>Spell</b>.<br><b>Skirmish:</b> Spot Gandalf to prevent all wounds to him. Any Shadow player may make you wound a minion to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fell");

		assertEquals("Long I Fell", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
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
		assertInHand(fell);
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
		scn.FreepsPlayCard(fell);
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();
		assertInDiscard(fell);
		assertEquals(0, scn.GetWoundsOn(toldea));

		// Toldea (12) beats Gandalf (7) without overwhelming him: the wound is prevented.
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
	public void OnlyGandalfIsProtectedWhereverHeStandsAndOnlyInTheSkirmishPhaseItIsPlayedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetFreepsCard("fell");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var skirmisher = scn.GetShadowCard("skirmisher");
		var skirmisher2 = scn.GetShadowCard("skirmisher2");
		scn.MoveCompanionsToTable(gandalf, avenger, aragorn);
		scn.MoveCardsToHand(fell);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, skirmisher, skirmisher2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gandalf, troop},
				new PhysicalCardImpl[]{avenger, skirmisher},
				new PhysicalCardImpl[]{aragorn, skirmisher2}
		);

		// Without the event: an Orkish Skirmisher (9) beats the Ent Avenger (6) and wounds Gandalf, who waits for his own skirmish.
		scn.FreepsResolveSkirmish(avenger);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		scn.PassSkirmishActions();
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(gandalf));
		scn.ShadowChooseCard(gandalf);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(avenger));

		// The event played in Aragorn's skirmish: "him" is Gandalf wherever he stands, and no one else.
		scn.FreepsResolveSkirmish(aragorn);
		assertFalse(scn.IsCharSkirmishing(gandalf));
		scn.FreepsPlayCard(fell);
		scn.ShadowChooseNo();
		assertInDiscard(fell);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		// The second Skirmisher (9) beats Aragorn (8): Aragorn takes his wound, and the one on Gandalf is prevented.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(gandalf));
		scn.ShadowChooseCard(gandalf);
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// Gandalf's own skirmish is the next skirmish phase: the Goblin Patrol Troop (13) beats him (7) and he is wounded.
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
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

		scn.FreepsPlayCard(fell);
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
