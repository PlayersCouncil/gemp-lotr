package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_182_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("devastation", "11_182");

					put("tyrannical", "11_204"); // Tyrannical Uruk: [uruk-hai] Uruk-hai, strength 14, vitality 3, damage +1; toil only as it is played
					put("hounding", "11_188");   // Hounding Uruk: [uruk-hai] Uruk-hai, strength 5, vitality 2, damage +1; +3 while a companion is exhausted
					put("saruman", "17_116");    // Saruman, Master of the White Hand: an [uruk-hai] Wizard, strength 8; companions resistance -1,
					                             // and a skirmish ability used only on purpose
					put("guard", "10_81");       // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9, damage +1, no other text

					// Conditions of each side, borne and in a support area, and two items that are none.
					put("stone", "1_314");       // Stone Trolls: a [shire] condition that plays on any character; +3 only against a Troll or an Uruk-hai
					put("mallorn", "1_54");      // Mallorn-trees: an [elven] condition in the support area; a fellowship ability only
					put("weary", "4_212");       // Weary: an [isengard] condition a companion bears; its trigger needs an [isengard] tracker
					put("beckoning", "12_83");   // The Beckoning Shadow: an [orc] condition in the support area; nothing at this board's burdens
					put("sword", "1_299");       // Hobbit Sword: a possession, on a Hobbit, no text
					put("coat", "2_105");        // Mithril-coat: an artifact, on Frodo; its text reaches only minions skirmishing Frodo

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");        // Gimli, Son of Gloin: strength 6, vitality 3, damage +1; a skirmish ability used only on purpose
					put("boromir", "1_96");      // Boromir, Lord of Gondor: strength 7, vitality 3; not overwhelmed unless tripled
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DevastationStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Devastation
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If your [uruk-hai] minion wins a skirmish, discard a condition from play.
		*/

		var scn = GetScenario();

		var devastation = scn.GetFreepsCard("devastation");

		assertEquals("Devastation", devastation.getBlueprint().getTitle());
		assertNull(devastation.getBlueprint().getSubtitle());
		assertFalse(devastation.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, devastation.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, devastation.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, devastation.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(devastation, Timeword.RESPONSE));
		assertEquals(1, devastation.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenAnUrukHaiWinsAConditionOfEitherSideBorneOrInASupportAreaIsDiscardedAndNoItem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var stone = scn.GetFreepsCard("stone");
		var mallorn = scn.GetFreepsCard("mallorn");
		var sword = scn.GetFreepsCard("sword");
		var coat = scn.GetFreepsCard("coat");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(gimli, stone);
		scn.AttachCardsTo(frodo, sword, coat);
		scn.MoveCardsToSupportArea(mallorn);

		var devastation = scn.GetShadowCard("devastation");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var weary = scn.GetShadowCard("weary");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(devastation);
		scn.AttachCardsTo(gimli, weary);
		scn.MoveCardsToSupportArea(beckoning);

		assertAttachedTo(stone, gimli);
		assertAttachedTo(weary, gimli);
		assertAttachedTo(sword, frodo);
		assertAttachedTo(coat, frodo);
		assertInZone(Zone.SUPPORT, mallorn, beckoning);
		assertInHand(devastation);

		scn.StartGame();
		scn.MoveMinionsToTable(tyrannical);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, tyrannical);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Nobody has won anything yet.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(devastation));

		// 14 against 8: the Uruk-hai wins, and Aragorn takes 1 wound + 1 for damage.
		scn.ShadowPass();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowPlayAvailable(devastation));

		scn.ShadowPlayCard(devastation);
		// A Free Peoples condition borne and one in a support area, a Shadow condition borne and one in a support
		// area; not a possession nor an artifact.
		assertTrue(scn.ShadowHasCardChoiceAvailable(stone, mallorn, weary, beckoning));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, coat));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(stone);

		assertInDiscard(stone);
		assertInDiscard(devastation);
		assertAttachedTo(weary, gimli);
		assertAttachedTo(sword, frodo);
		assertAttachedTo(coat, frodo);
		assertInZone(Zone.SUPPORT, mallorn, beckoning);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenAnUrukHaiCultureMinionWhoIsNoUrukHaiWinsItIsOfferedAndCanDiscardItsPlayersOwnCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(mallorn);

		var devastation = scn.GetShadowCard("devastation");
		var saruman = scn.GetShadowCard("saruman");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(devastation);
		scn.MoveCardsToSupportArea(beckoning);
		assertInZone(Zone.SUPPORT, mallorn, beckoning);
		assertInHand(devastation);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, saruman);
		scn.FreepsResolveSkirmish(boromir);

		// 8 against 7: Saruman, an [uruk-hai] Wizard, wins.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowPlayAvailable(devastation));

		scn.ShadowPlayCard(devastation);
		assertTrue(scn.ShadowHasCardChoiceAvailable(mallorn, beckoning));
		scn.ShadowChooseCard(beckoning);

		assertInDiscard(beckoning);
		assertInDiscard(devastation);
		assertInZone(Zone.SUPPORT, mallorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAnUrukHaiCultureMinionLosesNorWhenAnUrukHaiOfAnotherCultureWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(mallorn);

		var devastation = scn.GetShadowCard("devastation");
		var hounding = scn.GetShadowCard("hounding");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(devastation);
		assertInZone(Zone.SUPPORT, mallorn);
		assertInHand(devastation);

		scn.StartGame();
		scn.MoveMinionsToTable(hounding, guard);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, hounding}, new PhysicalCardImpl[]{gimli, guard});

		// 3 companions + 2 for the site: the event could be paid for.
		assertEquals(5, scn.GetTwilight());

		// 5 against 8: the Hounding Uruk, an [uruk-hai] minion, loses and lives through it.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(hounding));
		// The Shadow player is asked nothing: where the event can be played, he is asked here.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 9 against 6: the Guard, a [sauron] Uruk-hai, wins.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		assertInHand(devastation);
		assertInZone(Zone.SUPPORT, mallorn);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoConditionInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, sword);
		assertAttachedTo(sword, frodo);

		var devastation = scn.GetShadowCard("devastation");
		var tyrannical = scn.GetShadowCard("tyrannical");
		scn.MoveCardsToHand(devastation);
		assertInHand(devastation);

		scn.StartGame();
		scn.MoveMinionsToTable(tyrannical);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, tyrannical);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		// The event's target is no requirement: it is offered with nothing to discard.
		assertTrue(scn.ShadowPlayAvailable(devastation));
		scn.ShadowPlayCard(devastation);

		assertInDiscard(devastation);
		assertAttachedTo(sword, frodo);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
