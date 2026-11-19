package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_156_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("killthem", "4_156");
					put("killthem2", "4_156");
					put("killthem3", "4_156");
					put("runner", "4_193");     // Uruk Runner: [isengard] tracker, strength 8, vitality 2; stronger in a fierce skirmish only
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] tracker, strength 5, vitality 3; its response answers an unbound Hobbit killed
					put("scout", "2_47");       // Uruk Scout: [isengard] tracker, strength 7, Damage +1; its response answers an event that spots or exerts a ranger
					put("orcpursuer", "5_108"); // Orc Pursuer: [sauron] Orc tracker, strength 6; its trigger answers its own play, so only cheated in
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai who is no tracker, strength 5, Damage +1 and nothing else
					put("rest", "4_171");       // Rest While You Can: [isengard] search condition on a companion; answers its bearer killed
					put("weary", "4_212");      // Weary: [isengard] search condition on a companion or ally; its bearer exerts as he is assigned to an [isengard] tracker
					put("poison", "8_28");      // Spider Poison: a Shadow possession a companion bears, no search card; its bearer exerts as he is assigned

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally, vitality 2, who may exert to take part in skirmishes
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KillThemNowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Kill Them Now
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an [isengard] tracker strength +2 (or +4 if skirmishing a character bearing a search card).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("killthem");

		assertEquals("Kill Them Now", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlusTwoForATrackerNotSkirmishingABearerOfASearchCardAndPlusFourForOneWhoIsUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var killthem = scn.GetShadowCard("killthem");
		var killthem2 = scn.GetShadowCard("killthem2");
		var killthem3 = scn.GetShadowCard("killthem3");
		var runner = scn.GetShadowCard("runner");
		var pursuer = scn.GetShadowCard("pursuer");
		var scout = scn.GetShadowCard("scout");
		var orcpursuer = scn.GetShadowCard("orcpursuer");
		var savage = scn.GetShadowCard("savage");
		var rest = scn.GetShadowCard("rest");
		var poison = scn.GetShadowCard("poison");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		// Aragorn bears a search card; Gimli a Shadow card that is none.
		scn.AttachCardsTo(aragorn, rest);
		scn.AttachCardsTo(gimli, poison);
		scn.MoveCardsToHand(killthem, killthem2, killthem3);
		assertAttachedTo(rest, aragorn);
		assertAttachedTo(poison, gimli);
		assertInHand(killthem, killthem2, killthem3);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, pursuer, scout, orcpursuer, savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The phase before the skirmishes: not playable.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(killthem));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner}, new PhysicalCardImpl[]{aragorn, pursuer});
		// The Uruk Scout, an [isengard] tracker, is left out of every skirmish.
		scn.ShadowAssignToMinions(gimli, orcpursuer, savage);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(runner));
		assertEquals(5, scn.GetStrength(pursuer));
		scn.ShadowPlayCard(killthem);
		// Any [isengard] tracker: the Runner in this skirmish, the Pursuer whose skirmish is still to come and the
		// Scout in no skirmish; not the [sauron] tracker or the [isengard] Uruk-hai who is no tracker, though both are
		// in this skirmish.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, pursuer, scout));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(orcpursuer, savage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(runner);

		// Gimli bears a Shadow card, but no search card: 8 + 2
		assertEquals(10, scn.GetStrength(runner));
		assertInDiscard(killthem);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPass();
		scn.ShadowPlayCard(killthem2);
		scn.ShadowChooseCard(pursuer);
		// Assigned to Aragorn, who bears a search card, but not skirmishing him yet: 5 + 2
		assertEquals(7, scn.GetStrength(pursuer));
		assertEquals(10, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		// Both bonuses were gone with Gimli's skirmish.
		assertEquals(5, scn.GetStrength(pursuer));
		assertEquals(8, scn.GetStrength(runner));

		scn.ShadowPlayCard(killthem3);
		scn.ShadowChooseCard(pursuer);
		// Skirmishing Aragorn, who bears Rest While You Can: 5 + 4, not + 6
		assertEquals(9, scn.GetStrength(pursuer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void PlusFourForATrackerSkirmishingAnAllyWhoBearsASearchCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var killthem = scn.GetShadowCard("killthem");
		var scout = scn.GetShadowCard("scout");
		var weary = scn.GetShadowCard("weary");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(thrarin, weary);
		scn.MoveCardsToHand(killthem);
		assertAttachedTo(weary, thrarin);
		assertInHand(killthem);

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes until the regroup phase.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, scout);
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(scout));
		scn.ShadowPlayCard(killthem);

		// 7 + 4
		assertEquals(11, scn.GetStrength(scout));
		assertInDiscard(killthem);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
