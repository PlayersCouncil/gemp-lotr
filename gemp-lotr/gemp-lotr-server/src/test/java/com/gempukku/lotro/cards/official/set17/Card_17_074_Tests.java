package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_074_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cavalry", "17_74");
					put("cavalry2", "17_74");
					put("denizen", "11_115");     // Denizen of Khazad-dûm: [orc] Orc, strength 9; +2 only at an underground site
					// Threatening Warg: [orc] mount, +2 strength, +1 vitality, fierce; its play trigger wants 2 [orc] Orcs bearing no mount
					put("threatening", "17_91");
					put("vicious", "17_92");      // Vicious Warg: [orc] mount, +4 strength, +2 vitality, fierce; bearer of strength 9 or less
					put("gear", "17_90");         // Rider's Gear: an [orc] possession that is no mount (armor); a wound bar while mounted
					put("warwarg", "5_64");       // War-warg: an [isengard] mount, for a warg-rider; a skirmish response
					put("wargrider", "5_67");     // Warg-rider: [isengard] warg-rider Orc; fierce while not exhausted
					put("gilgalad", "9_15");      // Gil-galad, Elven High King: companion, strength 9; a regroup ability only
					put("gandalf", "6_30");       // Gandalf, Mithrandir: companion, strength 10; wounded each time the fellowship moves
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: companion, strength 8; a maneuver ability only
					// Thrarin, Dwarven Smith: ally; Maneuver: exert him to take part in archery fire and skirmishes until the regroup phase
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishCavalryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Orkish Cavalry
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Each time you play an [orc] mount, you may draw a card.<br><b>Assignment:</b> If this minion is mounted, exert it twice to assign it to a companion of strength 9 or more.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("cavalry");

		assertEquals("Orkish Cavalry", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachOrcMountPlayedLetsTheShadowPlayerDrawAndNoOtherPossessionOrMountDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cavalry = scn.GetShadowCard("cavalry");
		var denizen = scn.GetShadowCard("denizen");
		var threatening = scn.GetShadowCard("threatening");
		var vicious = scn.GetShadowCard("vicious");
		var gear = scn.GetShadowCard("gear");
		var warwarg = scn.GetShadowCard("warwarg");
		var wargrider = scn.GetShadowCard("wargrider");
		scn.MoveCardsToHand(cavalry, threatening, vicious, gear, warwarg);
		assertInHand(cavalry, threatening, vicious, gear, warwarg);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, wargrider);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		int deck = scn.GetShadowDeckCount();

		// Its own play: an [orc] card that is no mount.
		scn.ShadowPlayCard(cavalry);
		assertInZone(Zone.SHADOW_CHARACTERS, cavalry);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The Warg on the Cavalry leaves one [orc] Orc bearing no mount, so its own trigger is not offered.
		scn.ShadowPlayCard(threatening);
		scn.ShadowChooseCard(cavalry);
		assertAttachedTo(threatening, cavalry);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(cavalry));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second [orc] mount, of another title, on another minion: offered again.
		scn.ShadowPlayCard(vicious);
		assertAttachedTo(vicious, denizen);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(cavalry));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// An [orc] possession that is no mount.
		scn.ShadowPlayCard(gear);
		scn.ShadowChooseCard(denizen);
		assertAttachedTo(gear, denizen);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A mount of another culture.
		scn.ShadowPlayCard(warwarg);
		assertAttachedTo(warwarg, wargrider);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheDrawLeavesHandAndDeckAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cavalry = scn.GetShadowCard("cavalry");
		var threatening = scn.GetShadowCard("threatening");
		scn.MoveCardsToHand(threatening);
		assertInHand(threatening);

		scn.StartGame();
		scn.MoveMinionsToTable(cavalry);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(threatening);
		assertAttachedTo(threatening, cavalry);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(cavalry));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MountedItExertsTwiceToBeAssignedToACompanionOfStrength9OrMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cavalry = scn.GetShadowCard("cavalry");
		var cavalry2 = scn.GetShadowCard("cavalry2");
		var threatening = scn.GetShadowCard("threatening");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gilgalad, gandalf, aragorn);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		// A second Cavalry, unmounted, which neither pays nor is assigned.
		scn.MoveMinionsToTable(cavalry, cavalry2);
		// Vitality 2 + 1: he can exert twice.
		scn.AttachCardsTo(cavalry, threatening);
		assertAttachedTo(threatening, cavalry);
		// The ally fails only "companion": strength 4 + 6.
		scn.ApplyAdHocModifier(new StrengthModifier(null, thrarin, null, 6));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(gilgalad));
		assertEquals(10, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(10, scn.GetStrength(thrarin));
		// Thrarin takes part in skirmishes: by the rules the Shadow player could assign a minion to him.
		assertTrue(scn.ShadowCanAssign(thrarin));
		assertEquals(0, scn.GetWoundsOn(cavalry));
		assertEquals(0, scn.GetWoundsOn(cavalry2));
		assertTrue(scn.ShadowActionAvailable(cavalry));
		assertFalse(scn.ShadowActionAvailable(cavalry2));
		scn.ShadowUseCardAction(cavalry);

		assertEquals(2, scn.GetWoundsOn(cavalry));
		assertEquals(0, scn.GetWoundsOn(cavalry2));
		assertTrue(scn.ShadowHasCardChoiceAvailable(gilgalad, gandalf));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, thrarin, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gilgalad);

		assertTrue(scn.IsCharAssignedAgainst(gilgalad, cavalry));
		assertFalse(scn.IsCharAssigned(gandalf));
		assertFalse(scn.IsCharAssigned(cavalry2));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotMountedItIsNotOfferedThoughAnotherOrcIsMounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cavalry = scn.GetShadowCard("cavalry");
		var denizen = scn.GetShadowCard("denizen");
		var vicious = scn.GetShadowCard("vicious");
		var gear = scn.GetShadowCard("gear");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		scn.MoveCompanionsToTable(gilgalad);

		scn.StartGame();
		scn.MoveMinionsToTable(cavalry, denizen);
		// A possession that is no mount on the Cavalry, and a mount on the Denizen.
		scn.AttachCardsTo(cavalry, gear);
		scn.AttachCardsTo(denizen, vicious);
		assertAttachedTo(gear, cavalry);
		assertAttachedTo(vicious, denizen);
		// Vitality raised to 3 so that the exertions could be paid: only "mounted" keeps the ability off.
		scn.ApplyAdHocModifier(new VitalityModifier(null, cavalry, null, 1));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(3, scn.GetVitality(cavalry));
		assertFalse(scn.ShadowActionAvailable(cavalry));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void MountedWithTwoVitalityLeftItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cavalry = scn.GetShadowCard("cavalry");
		var threatening = scn.GetShadowCard("threatening");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		scn.MoveCompanionsToTable(gilgalad);

		scn.StartGame();
		scn.MoveMinionsToTable(cavalry);
		scn.AttachCardsTo(cavalry, threatening);
		assertAttachedTo(threatening, cavalry);
		scn.AddWoundsToChar(cavalry, 1);
		assertEquals(1, scn.GetWoundsOn(cavalry));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Vitality 3 with 1 wound: 2 left, too few to exert twice.
		assertEquals(2, scn.GetVitality(cavalry));
		assertFalse(scn.ShadowActionAvailable(cavalry));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void WithNoCompanionOfStrength9TheExertionsArePaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cavalry = scn.GetShadowCard("cavalry");
		var threatening = scn.GetShadowCard("threatening");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(cavalry);
		scn.AttachCardsTo(cavalry, threatening);
		assertAttachedTo(threatening, cavalry);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(cavalry));
		assertTrue(scn.ShadowActionAvailable(cavalry));
		scn.ShadowUseCardAction(cavalry);

		assertEquals(2, scn.GetWoundsOn(cavalry));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(cavalry));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
