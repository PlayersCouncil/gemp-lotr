package com.gempukku.lotro.cards.official.set12;

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

public class Card_12_072_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mount", "12_72");
					put("mouth", "12_73");      // The Mouth of Sauron, Messenger of Mordor: [men] Man minion, strength 9, vitality 3; his maneuver ability is used only on purpose
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: [men] Hobbit minion, strength 3, vitality 4; his abilities are used only on purpose
					put("men", "11_100");       // Strange-looking Men: [men] Man minion, strength 10, vitality 2; its ability is used only on purpose
					put("easterling", "4_224"); // Easterling Axeman: [raider] Man minion, strength 6, vitality 2; +3 and fierce only at 2 burdens, and there are none

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; his ability is used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: damage +1, strength 6; his ability is used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: an archer; his ability is used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSauronMouthScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mount", "12_72");
					put("mouth", "12_118");     // The Mouth of Sauron, Lieutenant of Barad-dur: the [sauron] Mouth, strength 9; an assignment ability used only on purpose
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: [men] Hobbit minion; his abilities are used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MessengersMountStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Messenger's Mount
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: +3
		 * Game Text: Bearer must be The Mouth of Sauron.<br>He is <b>fierce</b>.<br><b>Response:</b> If a [men] minion is about to take a wound, remove (3) to prevent that.
		*/

		var scn = GetScenario();

		var mount = scn.GetFreepsCard("mount");

		assertEquals("Messenger's Mount", mount.getBlueprint().getTitle());
		assertNull(mount.getBlueprint().getSubtitle());
		assertTrue(mount.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, mount.getBlueprint().getSide());
		assertEquals(Culture.MEN, mount.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, mount.getBlueprint().getCardType());
		assertTrue(mount.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, mount.getBlueprint().getTwilightCost());
		assertEquals(3, mount.getBlueprint().getStrength());
	}

	@Test
	public void CannotBePlayedUntilTheMouthOfSauronIsInPlayAndThenMakesHimFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mount = scn.GetShadowCard("mount");
		var mouth = scn.GetShadowCard("mouth");
		var ted = scn.GetShadowCard("ted");
		var men = scn.GetShadowCard("men");
		scn.MoveCardsToHand(mount);

		scn.StartGame();
		assertInHand(mount);
		scn.MoveMinionsToTable(ted, men);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Two [men] minions of other titles are no bearers.
		assertFalse(scn.ShadowPlayAvailable(mount));

		scn.MoveMinionsToTable(mouth);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(mount));
		assertFalse(scn.HasKeyword(mouth, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(ted, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(men, Keyword.FIERCE));

		// The Mouth is the only bearer, so it goes to him without a choice.
		scn.ShadowPlayCard(mount);
		assertAttachedTo(mount, mouth);
		assertTrue(scn.HasKeyword(mouth, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(ted, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(men, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlaysOnTheSauronCultureMouthOfSauronAndMakesHimFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSauronMouthScenario();

		var mount = scn.GetShadowCard("mount");
		var mouth = scn.GetShadowCard("mouth");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCardsToHand(mount);

		scn.StartGame();
		assertInHand(mount);
		scn.MoveMinionsToTable(mouth, ted);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(mouth, Keyword.FIERCE));
		assertTrue(scn.ShadowPlayAvailable(mount));

		scn.ShadowPlayCard(mount);
		assertAttachedTo(mount, mouth);
		assertTrue(scn.HasKeyword(mouth, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(ted, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RemovingThreePreventsTheWoundOfEachMenMinionAmongTheLosersOfASkirmishAndNotAnotherCultures() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mount = scn.GetShadowCard("mount");
		var mouth = scn.GetShadowCard("mouth");
		var ted = scn.GetShadowCard("ted");
		var easterling = scn.GetShadowCard("easterling");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, ted, easterling);
		scn.AttachCardsTo(mouth, mount);
		assertAttachedTo(mount, mouth);
		// Aragorn, 8 + 20, beats the three together, (9 + 3) + 3 + 6 = 21, and does not double them: all three
		// lose and are wounded together.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 20));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mouth);
		scn.ShadowAssignToMinions(aragorn, ted, easterling);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(6);

		assertEquals(28, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(mouth));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(easterling));
		assertEquals(6, scn.GetTwilight());
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mount));
		scn.ShadowChooseOptionalTrigger(mount);
		assertEquals(3, scn.GetTwilight());

		// The wound to be prevented is a [men] minion's, of either race, and not the [raider] Man's.
		assertTrue(scn.ShadowHasCardChoiceAvailable(mouth, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(easterling));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(ted);

		// The Mouth's wound is still to come, and is answered too.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mount));
		scn.ShadowChooseOptionalTrigger(mount);
		assertEquals(0, scn.GetTwilight());

		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(mouth));
		assertEquals(1, scn.GetWoundsOn(easterling));
		assertEquals(0, scn.GetTwilight());
		// The Mouth is fierce and lived: the game goes on to the fierce round.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ARaiderManLosingAloneIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mount = scn.GetShadowCard("mount");
		var mouth = scn.GetShadowCard("mouth");
		var easterling = scn.GetShadowCard("easterling");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, easterling);
		scn.AttachCardsTo(mouth, mount);
		assertAttachedTo(mount, mouth);
		scn.SkipToAssignments();
		// Aragorn, 8, beats the Easterling, 6, and does not double him; the Mouth is left to Gimli's skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, easterling},
				new PhysicalCardImpl[]{gimli, mouth});
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(easterling));
		assertEquals(3, scn.GetTwilight());
		scn.PassSkirmishActions();

		// The [raider] Man's wound is not answered, with the (3) there to pay.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mount));
		assertEquals(1, scn.GetWoundsOn(easterling));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AnArcheryWoundToAMenMinionIsPreventedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mount = scn.GetShadowCard("mount");
		var mouth = scn.GetShadowCard("mouth");
		var ted = scn.GetShadowCard("ted");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, ted);
		scn.AttachCardsTo(mouth, mount);
		assertAttachedTo(mount, mouth);
		scn.SkipToArcheryWounds();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(mouth));
		assertEquals(0, scn.GetWoundsOn(ted));
		// The fellowship's archery wound, placed by the Shadow player.
		scn.ShadowChooseCard(ted);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mount));
		scn.ShadowChooseOptionalTrigger(mount);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(mouth));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OnlyTheWoundItAnswersIsPreventedAndWithTwoInThePoolTheNextIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mount = scn.GetShadowCard("mount");
		var mouth = scn.GetShadowCard("mouth");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth);
		scn.AttachCardsTo(mouth, mount);
		assertAttachedTo(mount, mouth);
		// Gimli, 6 + 7, beats the Mouth, 9 + 3, and does not double him; damage +1 makes it two wounds.
		scn.ApplyAdHocModifier(new StrengthModifier(null, gimli, null, 7));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, mouth);
		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(5);

		assertEquals(13, scn.GetStrength(gimli));
		assertEquals(12, scn.GetStrength(mouth));
		assertEquals(0, scn.GetWoundsOn(mouth));
		assertEquals(5, scn.GetTwilight());
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mount));
		scn.ShadowChooseOptionalTrigger(mount);
		assertEquals(2, scn.GetTwilight());

		// The first wound is prevented; the second finds (2) in the pool, one short: not offered, and taken.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mount));
		assertEquals(1, scn.GetWoundsOn(mouth));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
