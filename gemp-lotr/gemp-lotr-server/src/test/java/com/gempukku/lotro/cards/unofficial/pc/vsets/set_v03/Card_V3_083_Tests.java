package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_083_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gcfa", "103_83");
					// The other beacons. No more than three War-beacons and two Northern Signal-fires may be in play.
					put("war1", "103_35");       // War-beacon: [gondor] possession, cost 1; to play, spot an unbound Man
					put("war2", "103_35");
					put("war3", "103_35");
					put("eilenach", "103_29");   // Northern Signal-fire, Flame of Eilenach: [gondor]; to play, hinder 2 beacons; a skirmish ability
					put("calenhad", "103_86");   // Northern Signal-fire, Flame of Calenhad: [rohan]; a maneuver ability

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man companion
					put("rider", "4_286");       // Rider of Rohan: [rohan] Man companion
					put("merchant", "7_97");     // Gondorian Merchant: [gondor] Man ally; his ability needs initiative
					put("aldor", "4_262");       // Aldor, Soldier of Edoras: [rohan] Man ally; an archery ability
					put("ghan", "0_56");         // Ghân-buri-Ghân: a [gandalf] Man companion
					put("pippin", "103_31");     // Pippin, Trollslayer: a [gondor] companion who is a Hobbit
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit, and a Man as well only while 3 [rohan]
					// companions (or Eowyn) can be spotted; his trigger answers a [rohan] Man exerting, and none does
					put("merry", "103_85");
					put("legolas", "1_50");      // Legolas, Greenleaf: an Elf, whom Brego could carry

					put("athelas1", "1_94");     // Athelas: [gondor] possession, cost 1. Bearer must be a [gondor] Man. Fellowship: discard it to heal a companion
					put("athelas2", "1_94");
					put("pipe", "1_91");         // Aragorn's Pipe: bearer must be a [gondor] companion
					put("mount1", "4_287");      // Rider's Mount: [rohan] possession, cost 1. Bearer must be a [rohan] Man
					put("mount2", "4_287");
					put("lance", "103_91");      // Vanguard's Lance: bearer must be a [rohan] companion
					put("brego", "4_263");       // Brego: [rohan] possession, cost 1. To play, spot a [rohan] Man; bearer must be a Man, Elf or Wizard; may heal bearer when played
					put("narsil", "9_34");       // Narsil, Blade of the Faithful: [gondor] artifact, cost 2. Bearer must be a [gondor] Man

					// In the discard pile and not items a Man can be given
					put("saga", "1_114");        // The Saga of Elendil: a condition. Bearer must be a [gondor] companion
					put("sting", "1_313");       // Sting: a possession. Bearer must be Frodo
					put("sapling", "9_35");      // Sapling of the White Tree: an artifact that plays to the support area
					put("iamnoman", "103_84");   // I Am No Man: an event

					put("walls", "7_111");       // Man the Walls: [gondor] condition in the support area
					put("slits", "5_80");        // Arrow-slits: [rohan] condition in the support area; a maneuver ability

					put("power", "51_136");      // Saruman's Power: exert an [isengard] minion to discard a Free Peoples condition and then hinder all other conditions
					put("uruk", "1_143");        // Troop of Uruk-hai: [isengard] minion, vitality 4
					put("runner", "1_178");      // Goblin Runner: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GondorCallsForAidStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Gondor Calls For Aid!
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Beacon. To play, hinder 4 beacons.
		 * 	This cannot be discarded or hindered.
		 * 	Your [Gondor] Men are considered [Rohan] Men.  Your [Rohan] Men are considered [Gondor] Men.
		 * 	Fellowship: Hinder X beacons to play X items from your discard pile on your Men.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gcfa");

		assertEquals("Gondor Calls For Aid!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.BEACON));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilFourBeaconsCanBeHinderedAndThenCostsThreeAndHindersThemAll() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gcfa = scn.GetFreepsCard("gcfa");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var war3 = scn.GetFreepsCard("war3");
		var eilenach = scn.GetFreepsCard("eilenach");
		var calenhad = scn.GetFreepsCard("calenhad");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(war1, war2, eilenach, calenhad);
		scn.MoveCardsToHand(gcfa, war3);
		// Four beacons in play, but one of them is hindered already.
		scn.HinderCard(war2);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(gcfa));

		// A third War-beacon, for (1), is the fourth beacon that can be hindered.
		scn.FreepsPlayCard(war3);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(gcfa));
		assertFalse(scn.IsHindered(war1));
		assertFalse(scn.IsHindered(war3));
		assertFalse(scn.IsHindered(eilenach));
		assertFalse(scn.IsHindered(calenhad));

		scn.FreepsPlayCard(gcfa);

		// 1 for the War-beacon + 3
		assertEquals(1 + 3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, gcfa);
		assertNotAttachedTo(gcfa, aragorn);
		assertFalse(scn.IsHindered(gcfa));
		assertTrue(scn.IsHindered(war1));
		assertTrue(scn.IsHindered(war2));
		assertTrue(scn.IsHindered(war3));
		assertTrue(scn.IsHindered(eilenach));
		assertTrue(scn.IsHindered(calenhad));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheFourBeaconsHinderedToPlayItAreChosenFromTheBeaconsOfEitherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gcfa = scn.GetFreepsCard("gcfa");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var war3 = scn.GetFreepsCard("war3");
		var eilenach = scn.GetFreepsCard("eilenach");
		var calenhad = scn.GetFreepsCard("calenhad");
		var walls = scn.GetFreepsCard("walls");
		scn.MoveCardsToSupportArea(war1, war2, war3, eilenach, calenhad, walls);
		scn.MoveCardsToHand(gcfa);

		scn.StartGame();

		scn.FreepsPlayCard(gcfa);

		assertTrue(scn.FreepsHasCardChoiceAvailable(war1, war2, war3, eilenach, calenhad));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(walls));
		assertEquals(4, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(war1, war2, eilenach, calenhad);

		assertTrue(scn.IsHindered(war1));
		assertTrue(scn.IsHindered(war2));
		assertTrue(scn.IsHindered(eilenach));
		assertTrue(scn.IsHindered(calenhad));
		assertFalse(scn.IsHindered(war3));
		assertFalse(scn.IsHindered(walls));
		assertInZone(Zone.SUPPORT, gcfa);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNeitherTheConditionSarumansPowerDiscardsNorAmongThoseItThenHinders() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gcfa = scn.GetFreepsCard("gcfa");
		var walls = scn.GetFreepsCard("walls");
		var slits = scn.GetFreepsCard("slits");
		scn.MoveCardsToSupportArea(gcfa, walls, slits);

		var uruk = scn.GetShadowCard("uruk");
		var power = scn.GetShadowCard("power");
		scn.MoveCardsToHand(power);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(power));
		scn.ShadowPlayCard(power);

		// The two other Free Peoples conditions may be discarded; this one may not.
		assertEquals(1, scn.GetWoundsOn(uruk));
		assertTrue(scn.ShadowHasCardChoiceAvailable(walls, slits));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gcfa));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(walls);

		assertInZone(Zone.DISCARD, walls);
		assertInZone(Zone.SUPPORT, gcfa, slits);
		// "All other conditions" are then hindered: Arrow-slits is, this card is not.
		assertTrue(scn.IsHindered(slits));
		assertFalse(scn.IsHindered(gcfa));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotAmongTheBeaconsThatMayBeHinderedToPlayAnotherBeacon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gcfa = scn.GetFreepsCard("gcfa");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var war3 = scn.GetFreepsCard("war3");
		var eilenach = scn.GetFreepsCard("eilenach");
		scn.MoveCardsToSupportArea(gcfa, war1, war2, war3);
		scn.MoveCardsToHand(eilenach);

		scn.StartGame();

		// "To play, hinder 2 beacons."
		scn.FreepsPlayCard(eilenach);

		assertTrue(scn.FreepsHasCardChoiceAvailable(war1, war2, war3));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gcfa));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(war1, war2);

		assertTrue(scn.IsHindered(war1));
		assertTrue(scn.IsHindered(war2));
		assertFalse(scn.IsHindered(war3));
		assertFalse(scn.IsHindered(gcfa));
		assertInZone(Zone.SUPPORT, eilenach);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RohanMenAreGondorMenOnceItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gcfa = scn.GetFreepsCard("gcfa");
		var rider = scn.GetFreepsCard("rider");
		var merry = scn.GetFreepsCard("merry");
		var ghan = scn.GetFreepsCard("ghan");
		var merchant = scn.GetFreepsCard("merchant");
		var aldor = scn.GetFreepsCard("aldor");
		var athelas1 = scn.GetFreepsCard("athelas1");
		var athelas2 = scn.GetFreepsCard("athelas2");
		var pipe = scn.GetFreepsCard("pipe");
		// The one [rohan] companion who is no Man is Merry, Master Holbytla, and he is a Man while there are three
		// [rohan] companions. So the [gondor] Man here is an ally: with this card in play a [gondor] Man companion
		// would be a [rohan] companion too, the third beside the Rider and Merry.
		scn.MoveCompanionsToTable(rider, merry, ghan);
		scn.MoveAlliesToTable(merchant, aldor);
		scn.MoveCardsToSupportArea(scn.GetFreepsCard("war1"), scn.GetFreepsCard("war2"), scn.GetFreepsCard("war3"), scn.GetFreepsCard("calenhad"));
		scn.MoveCardsToHand(gcfa, athelas1, athelas2, pipe);

		scn.StartGame();

		assertFalse(scn.IsRace(merry, Race.MAN));
		// Aragorn's Pipe: bearer must be a [gondor] companion, and there is none.
		assertFalse(scn.FreepsPlayAvailable(pipe));

		// Athelas: bearer must be a [gondor] Man. The Merchant is the only one, so nothing is asked: neither
		// [rohan] Man may bear it yet.
		scn.FreepsPlayCard(athelas1);
		assertAttachedTo(athelas1, merchant);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The four beacons are the only ones, so they are hindered without a choice.
		scn.FreepsPlayCard(gcfa);
		assertInZone(Zone.SUPPORT, gcfa);
		// Still two [rohan] companions: the Merchant, a [rohan] Man now, is an ally.
		assertFalse(scn.IsRace(merry, Race.MAN));

		// The [rohan] Men, companion and ally, may now bear it; a Man of another culture and a [rohan] companion
		// who is no Man may not. The Merchant is still a [gondor] Man.
		scn.FreepsPlayCard(athelas2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merchant, rider, aldor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ghan, merry));
		scn.FreepsChooseCard(aldor);
		assertAttachedTo(athelas2, aldor);

		// The Pipe can be played now. The Rider is the only [gondor] companion, so nothing is asked: Merry is a
		// [rohan] companion and no Man, and has not become a [gondor] companion.
		assertTrue(scn.FreepsPlayAvailable(pipe));
		scn.FreepsPlayCard(pipe);
		assertAttachedTo(pipe, rider);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void GondorMenAreRohanMenOnceItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gcfa = scn.GetFreepsCard("gcfa");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetFreepsCard("rider");
		var pippin = scn.GetFreepsCard("pippin");
		var ghan = scn.GetFreepsCard("ghan");
		var merchant = scn.GetFreepsCard("merchant");
		var aldor = scn.GetFreepsCard("aldor");
		var mount1 = scn.GetFreepsCard("mount1");
		var mount2 = scn.GetFreepsCard("mount2");
		var lance = scn.GetFreepsCard("lance");
		scn.MoveCompanionsToTable(aragorn, rider, pippin, ghan);
		scn.MoveAlliesToTable(merchant, aldor);
		scn.MoveCardsToSupportArea(scn.GetFreepsCard("war1"), scn.GetFreepsCard("war2"), scn.GetFreepsCard("war3"), scn.GetFreepsCard("calenhad"));
		scn.MoveCardsToHand(gcfa, mount1, mount2, lance);

		scn.StartGame();

		// Rider's Mount: bearer must be a [rohan] Man.
		scn.FreepsPlayCard(mount1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, aldor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, merchant, ghan, pippin));
		scn.FreepsChooseCard(aldor);

		scn.FreepsPlayCard(gcfa);
		assertInZone(Zone.SUPPORT, gcfa);

		// The [gondor] Men, companion and ally, may now bear one; a Man of another culture may not.
		scn.FreepsPlayCard(mount2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, aragorn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ghan, pippin));
		scn.FreepsChooseCard(merchant);
		assertAttachedTo(mount2, merchant);

		// Vanguard's Lance: bearer must be a [rohan] companion. Pippin is a [gondor] companion but a Hobbit.
		scn.FreepsPlayCard(lance);
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, ghan));
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(lance, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityHindersABeaconToPlayAnItemFromTheDiscardPileOnAMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gcfa = scn.GetFreepsCard("gcfa");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var calenhad = scn.GetFreepsCard("calenhad");
		var walls = scn.GetFreepsCard("walls");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetFreepsCard("rider");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var mount = scn.GetFreepsCard("mount1");
		var saga = scn.GetFreepsCard("saga");
		var sting = scn.GetFreepsCard("sting");
		var sapling = scn.GetFreepsCard("sapling");
		var iamnoman = scn.GetFreepsCard("iamnoman");
		scn.MoveCompanionsToTable(aragorn, rider);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(gcfa, war1, war2, calenhad, walls);
		scn.HinderCard(war2);
		// An artifact and a possession that Men can bear; a condition Aragorn could bear, a possession for Frodo
		// (who is in play), an artifact for the support area and an event.
		scn.MoveCardsToDiscard(narsil, mount, saga, sting, sapling, iamnoman);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(gcfa));

		scn.FreepsUseCardAction(gcfa);

		// Beacons of both cultures; not this card, not a hindered beacon, not another condition. At least one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(war1, calenhad));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gcfa, war2, walls));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(war1);

		assertTrue(scn.IsHindered(war1));
		assertFalse(scn.IsHindered(calenhad));
		assertTrue(scn.FreepsHasCardChoiceAvailable(narsil, mount));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(saga, sting, sapling, iamnoman));
		scn.FreepsChooseCardBPFromSelection(narsil);

		// Narsil must be borne by a [gondor] Man: a companion, an ally, and the Rider, who now is one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, merchant, rider));
		scn.FreepsChooseCard(rider);

		assertAttachedTo(narsil, rider);
		// Narsil costs (2).
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, mount, saga, sting, sapling, iamnoman);
		assertFalse(scn.IsHindered(calenhad));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithXOfTwoTwoBeaconsAreHinderedAndTwoItemsArePlayedEachOnAnyMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gcfa = scn.GetFreepsCard("gcfa");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var war3 = scn.GetFreepsCard("war3");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetFreepsCard("rider");
		var legolas = scn.GetFreepsCard("legolas");
		var brego = scn.GetFreepsCard("brego");
		var athelas = scn.GetFreepsCard("athelas1");
		var mount = scn.GetFreepsCard("mount1");
		scn.MoveCompanionsToTable(aragorn, rider, legolas);
		scn.MoveCardsToSupportArea(gcfa, war1, war2, war3);
		scn.MoveCardsToDiscard(brego, athelas, mount);

		scn.StartGame();

		scn.FreepsUseCardAction(gcfa);

		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(war1, war2);

		assertTrue(scn.FreepsHasCardChoiceAvailable(brego, athelas, mount));
		scn.FreepsChooseCardBPFromSelection(brego);
		// Brego's own text would let an Elf bear him; this card plays items on Men.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, rider));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas));
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(brego, aragorn);
		// "When you play Brego, you may heal bearer."  Aragorn has no wound: the offer costs nothing and would do
		// nothing, and is not made (Q-0034; withheld by the engine since Q-0224).
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(brego));

		assertTrue(scn.FreepsHasCardChoiceAvailable(athelas, mount));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(brego));
		scn.FreepsChooseCardBPFromSelection(athelas);
		// The second item may go on the Man who was given the first.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, rider));
		scn.FreepsChooseCard(aragorn);

		assertAttachedTo(athelas, aragorn);
		assertInZone(Zone.DISCARD, mount);
		assertTrue(scn.IsHindered(war1));
		assertTrue(scn.IsHindered(war2));
		assertFalse(scn.IsHindered(war3));
		// 1 for Brego + 1 for Athelas
		assertEquals(1 + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedUntilAnItemAManCanBearIsInTheDiscardPileAndThenInTheFellowshipPhaseOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gcfa = scn.GetFreepsCard("gcfa");
		var war1 = scn.GetFreepsCard("war1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas1");
		var saga = scn.GetFreepsCard("saga");
		var sting = scn.GetFreepsCard("sting");
		var sapling = scn.GetFreepsCard("sapling");
		var iamnoman = scn.GetFreepsCard("iamnoman");
		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AttachCardsTo(aragorn, athelas);
		scn.MoveCardsToSupportArea(gcfa, war1);
		scn.MoveCardsToDiscard(saga, sting, sapling, iamnoman);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		// A beacon to hinder, but nothing in the discard pile that is an item for a Man.
		assertFalse(scn.FreepsActionAvailable(gcfa));

		// Athelas is discarded for its own ability, which heals Aragorn.
		scn.FreepsUseCardAction(athelas);
		assertInZone(Zone.DISCARD, athelas);
		assertEquals(0, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsActionAvailable(gcfa));

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gcfa));
		assertFalse(scn.IsHindered(war1));
		assertInZone(Zone.DISCARD, athelas);
	}

	@Test
	public void NotOfferedWhileEveryOtherBeaconIsHinderedBecauseItCannotHinderItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gcfa = scn.GetFreepsCard("gcfa");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var mount = scn.GetFreepsCard("mount1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(gcfa, war1);
		scn.HinderCard(war1);
		scn.MoveCardsToHand(war2);
		scn.MoveCardsToDiscard(mount);

		scn.StartGame();

		assertFalse(scn.FreepsActionAvailable(gcfa));

		scn.FreepsPlayCard(war2);
		assertEquals(1, scn.GetTwilight());

		assertTrue(scn.FreepsActionAvailable(gcfa));
		scn.FreepsUseCardAction(gcfa);

		// One beacon that can be hindered, one item, one Man (a [rohan] Man for the Rider's Mount): nothing to choose.
		assertTrue(scn.IsHindered(war2));
		assertFalse(scn.IsHindered(gcfa));
		assertAttachedTo(mount, aragorn);
		// 1 for the War-beacon + 1 for the Rider's Mount
		assertEquals(1 + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void XIsNoMoreThanTheItemsInTheDiscardPileThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gcfa = scn.GetFreepsCard("gcfa");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var war3 = scn.GetFreepsCard("war3");
		var aragorn = scn.GetFreepsCard("aragorn");
		var mount = scn.GetFreepsCard("mount1");
		var saga = scn.GetFreepsCard("saga");
		var sting = scn.GetFreepsCard("sting");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(gcfa, war1, war2, war3);
		// One item a Man can bear, beside a condition and a possession for Frodo.
		scn.MoveCardsToDiscard(mount, saga, sting);

		scn.StartGame();

		scn.FreepsUseCardAction(gcfa);

		// Three beacons could be hindered, but only one item can be played.
		assertTrue(scn.FreepsHasCardChoiceAvailable(war1, war2, war3));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(war1);

		assertTrue(scn.IsHindered(war1));
		assertFalse(scn.IsHindered(war2));
		assertFalse(scn.IsHindered(war3));
		assertAttachedTo(mount, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
