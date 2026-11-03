package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_064_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warg", "5_64");
					put("warg2", "5_64");
					put("rider", "5_53");       // Isengard Rider: warg-rider, strength 8, vitality 3; fierce while not exhausted
					put("rider2", "5_67");      // Warg-rider: warg-rider, strength 5, vitality 2; fierce while not exhausted
					put("shaman", "3_59");      // Isengard Shaman: an [isengard] Orc who is no warg-rider, strength 5, vitality 2;
					                            // Response: if an [isengard] Orc is about to take a wound, remove (2) to prevent it

					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; Skirmish: exert Gimli to make him strength +2
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("axestrike", "1_3");    // Axe Strike: Skirmish event, a Dwarf strength +2 (+3 with a [dwarven] hand weapon)
					put("slayer", "1_153");     // Uruk Slayer: an [isengard] Uruk-hai, strength 9, damage +1; Skirmish: remove (1) to make himself strength +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WarwargStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: War-warg
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: +4
		 * Vitality: +2
		 * Game Text: Bearer must be a warg-rider.<br><b>Response:</b> If a skirmish special ability is used in a skirmish involving bearer, exert bearer to cancel that action.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warg");

		assertEquals("War-warg", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnAWargRiderOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var rider = scn.GetShadowCard("rider");
		var rider2 = scn.GetShadowCard("rider2");
		var shaman = scn.GetShadowCard("shaman");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(warg);
		assertInHand(warg);

		scn.StartGame();
		scn.SetTwilight(4);
		// Laid before the Shadow phase begins, so that its first choice of actions takes them in.
		scn.MoveMinionsToTable(rider, rider2, shaman);
		scn.FreepsPass();

		assertInHand(warg);
		assertTrue(scn.ShadowPlayAvailable(warg));
		scn.ShadowPlayCard(warg);
		// Two warg-riders, and an [isengard] Orc who is none.
		assertTrue(scn.ShadowHasCardChoiceAvailable(rider, rider2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shaman, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(rider);

		assertAttachedTo(warg, rider);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ExertsBearerToCancelASkirmishSpecialAbilityUsedInHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var rider = scn.GetShadowCard("rider");
		var shaman = scn.GetShadowCard("shaman");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, shaman);
		scn.AttachCardsTo(rider, warg);
		assertAttachedTo(warg, rider);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, rider);
		// The Shaman fights nobody.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(shaman));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warg));
		scn.ShadowAcceptOptionalTrigger();

		// The bearer pays, and the minion beside him does not.
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(shaman));
		// Cancelled: Gimli's exertion stays paid and he is not strength +2.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnswersASitesSkirmishAbilityUsedInItsBearersSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var rider = scn.GetShadowCard("rider2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.AttachCardsTo(rider, warg);
		assertAttachedTo(warg, rider);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rider);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(8, scn.GetStrength(aragorn));

		// Ettenmoors, site 2: "Skirmish: Exert your companion or minion to make that character strength +2."
		// A site's skirmish ability, used by no character, in the bearer's skirmish.
		var ettenmoors = scn.GetCurrentSite();
		scn.FreepsUseCardAction(ettenmoors);
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warg));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedForASkirmishEventInItsBearersSkirmishNorForASkirmishAbilityInAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var warg2 = scn.GetShadowCard("warg2");
		var rider = scn.GetShadowCard("rider");
		var rider2 = scn.GetShadowCard("rider2");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axestrike = scn.GetFreepsCard("axestrike");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(axestrike);
		assertInHand(axestrike);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, rider2);
		// A second War-warg, on the warg-rider of the other skirmish.
		scn.AttachCardsTo(rider, warg);
		scn.AttachCardsTo(rider2, warg2);
		assertAttachedTo(warg, rider);
		assertAttachedTo(warg2, rider2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, rider}, new PhysicalCardImpl[]{aragorn, rider2});

		// Another skirmish is fought first, with this War-warg's bearer assigned and waiting for his own:
		// Ettenmoors' skirmish ability is used where he is not.  The other War-warg, whose bearer is there, answers.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(rider2));
		assertEquals(8, scn.GetStrength(aragorn));
		var ettenmoors = scn.GetCurrentSite();
		scn.FreepsUseCardAction(ettenmoors);
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(warg));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warg2));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(rider2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 10 against 5 + 4 for the Warg-rider: Aragorn wins.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(rider2));

		// The bearer's skirmish: a skirmish event, which is no special ability.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));
		assertInHand(axestrike);
		scn.FreepsPlayCard(axestrike);
		assertEquals(8, scn.GetStrength(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(warg));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnswersASkirmishSpecialAbilityTheShadowPlayerUsesInItsBearersSkirmishToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var rider = scn.GetShadowCard("rider");
		var slayer = scn.GetShadowCard("slayer");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, slayer);
		scn.AttachCardsTo(rider, warg);
		assertAttachedTo(warg, rider);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, rider);
		// The Slayer fights nobody.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(slayer));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(slayer));

		// Used with the response declined, his ability works: the control.
		scn.ShadowUseCardAction(slayer);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warg));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(10, scn.GetStrength(slayer));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(slayer));
		scn.ShadowUseCardAction(slayer);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warg));
		scn.ShadowAcceptOptionalTrigger();

		// Cancelled: the Slayer's price stays paid and he is not strength +1 again.
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertEquals(10, scn.GetStrength(slayer));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileItsBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var rider = scn.GetShadowCard("rider");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.AttachCardsTo(rider, warg);
		assertAttachedTo(warg, rider);
		// Vitality 3 + 2 for the War-warg, with 4 wounds: the bearer cannot exert.
		scn.AddWoundsToChar(rider, 4);
		assertEquals(4, scn.GetWoundsOn(rider));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, rider);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(4, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(warg));
		assertEquals(4, scn.GetWoundsOn(rider));
		assertEquals(8, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
