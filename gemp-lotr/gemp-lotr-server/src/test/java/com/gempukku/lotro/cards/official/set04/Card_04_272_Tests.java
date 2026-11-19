package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_272_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "4_272");
					// Eowyn, Sister-daughter of Theoden: [rohan] Man companion; her response wants her exhausted in a
					// skirmish
					put("eowyn", "4_271");
					// Ceorl, Weary Horseman: [rohan] Man companion who is no Eowyn; his response answers only a site
					// about to be taken
					put("ceorl", "4_264");
					// Guma, Plains Farmer: [rohan] Man ally, Villager, home 4T, cost 1 with nothing to spot; discarded
					// only if an opponent controls his home site, and his fellowship ability is used only on purpose
					put("guma", "4_277");
					// Rohirrim Herdsman: [rohan] Man ally, Villager, not unique; "To play, spot 2 [rohan] Men", so he is
					// cheated in; a regroup ability used only on purpose
					put("herdsman", "7_247");
					// Aldor, Soldier of Edoras: [rohan] Man ally who is no villager; an archery ability used only on
					// purpose
					put("aldor", "4_262");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EowynsSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Éowyn's Sword
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +3
		 * Game Text: Bearer must be Éowyn.
		 * 	While you can spot a villager, Éowyn is <b>damage +1</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Éowyn's Sword", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
	}

	@Test
	public void IsPlayedOnEowynAndNotOnAnotherRohanCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var eowyn = scn.GetFreepsCard("eowyn");
		var ceorl = scn.GetFreepsCard("ceorl");
		scn.MoveCompanionsToTable(eowyn, ceorl);
		scn.MoveCardsToHand(sword);

		scn.StartGame();

		assertInZone(Zone.HAND, sword);
		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);

		// Eowyn is the one bearer it may go on, and is taken unasked: Ceorl, a [rohan] Man companion of another
		// title, is no candidate.
		assertAttachedTo(sword, eowyn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutEowyn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var ceorl = scn.GetFreepsCard("ceorl");
		scn.MoveCompanionsToTable(ceorl);
		scn.MoveCardsToHand(sword);

		scn.StartGame();

		assertInZone(Zone.HAND, sword);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(sword));
	}

	@Test
	public void EowynIsDamagePlus1OnceAVillagerIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var eowyn = scn.GetFreepsCard("eowyn");
		var ceorl = scn.GetFreepsCard("ceorl");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(eowyn, ceorl);
		scn.AttachCardsTo(eowyn, sword);
		scn.MoveCardsToHand(guma);

		scn.StartGame();

		assertAttachedTo(sword, eowyn);
		assertFalse(scn.HasKeyword(eowyn, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(ceorl, Keyword.DAMAGE));

		scn.FreepsPlayCard(guma);
		assertInZone(Zone.SUPPORT, guma);
		assertTrue(scn.HasKeyword(eowyn, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(eowyn, Keyword.DAMAGE));
		// Ceorl does not bear it.
		assertFalse(scn.HasKeyword(ceorl, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanAllyWhoIsNoVillagerDoesNotSwitchItOnAndAnotherVillagerDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var eowyn = scn.GetFreepsCard("eowyn");
		var aldor = scn.GetFreepsCard("aldor");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(eowyn);
		scn.AttachCardsTo(eowyn, sword);
		scn.MoveAlliesToTable(aldor);

		scn.StartGame();

		// Aldor is a [rohan] Man ally and no villager.
		assertAttachedTo(sword, eowyn);
		assertInZone(Zone.SUPPORT, aldor);
		assertFalse(scn.HasKeyword(eowyn, Keyword.DAMAGE));

		scn.MoveAlliesToTable(herdsman);
		assertInZone(Zone.SUPPORT, herdsman);
		assertEquals(1, scn.GetKeywordCount(eowyn, Keyword.DAMAGE));
	}
}
